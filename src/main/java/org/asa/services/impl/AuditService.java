package org.asa.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.asa.exceptions.BankAccountRuntimeException;
import org.asa.models.AuditLog;
import org.asa.models.Batch;
import org.asa.models.Transaction;
import org.asa.repositories.AuditLogRepository;
import org.asa.repositories.BatchRepository;
import org.asa.repositories.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Transactional
class AuditService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditService.class);

    private static final double TRANSACTION_AMOUNT_THRESHOLD = 1_000_000; // £1,000,000 Max for a batch
    private static final int SUBMISSION_SIZE = 1_000; // Max transactions per audit log
    private final ObjectMapper mapper = new ObjectMapper();

    private final TransactionRepository transactionRepository;
    private final BatchRepository batchRepository;
    private final AuditLogRepository auditLogRepository;

    public AuditService(TransactionRepository transactionRepository, BatchRepository batchRepository,
                        AuditLogRepository auditLogRepository) {
        this.transactionRepository = transactionRepository;
        this.batchRepository = batchRepository;
        this.auditLogRepository = auditLogRepository;
    }

    public void auditLogs() {
        int page = 0;
        PageRequest pageRequest = PageRequest.of(page, SUBMISSION_SIZE);
        Page<Transaction> transactionPage;

        do {
            transactionPage = transactionRepository.findUnbatchedTransactions(pageRequest);
            List<Transaction> transactions = transactionPage.getContent();
            if (transactions.isEmpty()) {
                return;
            }
            processBatch(transactions);
            pageRequest = PageRequest.of(++page, SUBMISSION_SIZE);
        } while (!transactionPage.isEmpty());
    }

    private void processBatch(List<Transaction> unbatchedTransactions) {

        // Create a new audit log and batch collection which will be associated with it
        AuditLog auditLog = new AuditLog();
        auditLogRepository.save(auditLog);
        List<Batch> processedBatches = new ArrayList<>();

        double batchTotal = 0; // Total amount in the current batch
        int batchTransactionCount = 0; // Number of transactions in the current batch
        int entryInLogs = 0; // Number of transactions in the current log
        double entryTotalAmount = 0; // Total amount in the current log

        // Create first batch
        Batch batch = new Batch(UUID.randomUUID().toString(), 0, 0, auditLog);
        batch = batchRepository.save(batch);

        // Iterate through the transactions and create batches as per the rules
        for (Transaction transaction : unbatchedTransactions) {

            // Seems unnecessary to check submission size here as we're only passing the required number
            // But let's keep it for safety :-)
            if (entryInLogs >= SUBMISSION_SIZE) {
                break;
            }

            // Since there is amount threshold, if the limit is exceeded, we need to create a new batch
            if (batchTotal + Math.abs(transaction.getAmount()) > TRANSACTION_AMOUNT_THRESHOLD) {

                // Save the current attributes in batch
                batch.setTransactionCount(batchTransactionCount);
                batch.setTotalValue(batchTotal);

                // Add the batch to the collection to be persisted later
                processedBatches.add(batch);

                // Create a new batch for the next set of transactions and reset related attributes
                batch = new Batch(UUID.randomUUID().toString(), 0, 0, auditLog);
                batch = batchRepository.save(batch);
                batchTransactionCount = 0;
                batchTotal = 0;
            }

            // Add the batch to the transaction as foreign entity
            transaction.setBatch(batch);

            // Add the absolute value of the transaction amount to the batch total
            batchTotal += Math.abs(transaction.getAmount());
            batchTransactionCount++;
            entryInLogs++;

            // Add the absolute value of the transaction amount to the audit entry total
            entryTotalAmount += Math.abs(transaction.getAmount());
        }

        // Set audit log attributes
        auditLog.setBatchCount(processedBatches.size());
        auditLog.setTotalAmount(entryTotalAmount);

        // Save all the entities in the database
        auditLogRepository.save(auditLog);
        batchRepository.saveAll(processedBatches);
        transactionRepository.saveAll(unbatchedTransactions);

        // Log the audit log and batch details
        LOGGER.info("Audit Log Submission: {}", auditLog);

        List<Map<String, String>> submission = new ArrayList<>();
        processedBatches.forEach(b -> {
            Map<String, String> batchMap = Map.of(
                    "countOfTransactions:", String.valueOf(b.getTransactionCount()),
                    "totalValueOfAllTransactions:", String.valueOf(b.getTotalValue())
            );
            submission.add(batchMap);
        });
        try {
            LOGGER.info("Audit Log Submission: { \"submission\": {{}}",
                    mapper.writeValueAsString(Collections.singletonMap("batches",submission)));
        } catch (JsonProcessingException e) {
            throw new BankAccountRuntimeException(e.getMessage());
        }
    }
}