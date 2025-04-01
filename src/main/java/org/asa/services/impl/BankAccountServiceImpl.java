package org.asa.services.impl;

import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.asa.exceptions.BankAccountRuntimeException;
import org.asa.models.Balance;
import org.asa.models.Transaction;
import org.asa.repositories.BalanceRepository;
import org.asa.repositories.TransactionRepository;
import org.asa.services.BankAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BankAccountServiceImpl implements BankAccountService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BankAccountServiceImpl.class);
    private final TransactionRepository transactionRepository;
    private final BalanceRepository balanceRepository;
    private final Random random = new Random();

    @Transactional
    @Scheduled(fixedRate = 40) // Generates 50 transactions per second - 25 credits and 25 debits
    public void generateTransactions() {
        double credit = 200 + (500_000 - 200) * random.nextDouble(); // Random amount between £200 and £500,000
        double debit = - (200 + (500_000 - 200) * random.nextDouble()); // Random amount between £200 and £500,000

        processTransaction(new Transaction(UUID.randomUUID().toString(), credit)); // Process credit transaction
        processTransaction(new Transaction(UUID.randomUUID().toString(), debit));  // Process debit transaction
    }

    @Transactional
    public void processTransaction(Transaction transaction) {
        transactionRepository.save(transaction);

        boolean updated = false; // for optimistic locking
        int retryCount = 0; // Retry count for optimistic locking

        while (!updated && retryCount < 5) { // Retry on optimistic locking failure

            try {
                long currentTime = System.currentTimeMillis();

                // Fetch the latest balance or create a new one if it doesn't exist
                Balance latestBalance = balanceRepository.findById(1L)
                        .orElseGet(() -> new Balance(1L, 0.0, currentTime));

                // Check if the transaction would result in a negative balance
                if (!latestBalance.canApplyTransaction(transaction.getAmount())) {
                    LOGGER.info("Transaction declined: Insufficient funds. TransactionId: {}, Amount: {}, Available Balance: {}",
                            transaction.getTransactionId(), transaction.getAmount(), latestBalance.getAmount());
                    return; // Exit without applying the transaction
                }

                latestBalance.applyTransaction(transaction.getAmount());
                balanceRepository.save(latestBalance);
                updated = true; // Unlock after successful update

            } catch (OptimisticLockException e) {
                retryCount++;
                LOGGER.error("Optimistic Locking Failure. Retrying... Attempt: {}", retryCount);
            }
        }

        if (!updated) {
            throw new BankAccountRuntimeException("Failed to process transaction after multiple attempts");
        }
    }

    public double retrieveBalance() {
        // Fetch the latest balance or return 0.0 if it doesn't exist
        return balanceRepository.findById(1L)
                .map(Balance::getAmount)
                .orElse(0.0);
    }
}
