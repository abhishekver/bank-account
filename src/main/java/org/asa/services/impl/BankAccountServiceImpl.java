package org.asa.services.impl;

import jakarta.transaction.Transactional;
import org.asa.models.Balance;
import org.asa.models.Transaction;
import org.asa.repositories.BalanceRepository;
import org.asa.repositories.TransactionRepository;
import org.asa.services.BankAccountService;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class BankAccountServiceImpl implements BankAccountService {

    private final TransactionRepository transactionRepository;
    private final BalanceRepository balanceRepository;

    public BankAccountServiceImpl(TransactionRepository transactionRepository, BalanceRepository balanceRepository) {
        this.transactionRepository = transactionRepository;
        this.balanceRepository = balanceRepository;
    }

    public void processTransaction(Transaction transaction) {
        transactionRepository.save(transaction);

        Balance latestBalance = balanceRepository.findById(1L).orElse(
                new Balance(1L, 0.0, System.currentTimeMillis()));
        latestBalance.updateBalance(transaction.getAmount());
        latestBalance.setUpdatedAt(System.currentTimeMillis());
        Balance newBalance = balanceRepository.save(latestBalance);

        balanceRepository.save(newBalance);
    }

    public double retrieveBalance() {
        return balanceRepository.findById(1L).orElse(
                new Balance(1L, 0.0, System.currentTimeMillis())).getAmount();
    }
}
