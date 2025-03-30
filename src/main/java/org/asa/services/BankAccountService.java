package org.asa.services;

import org.asa.models.Transaction;

public interface BankAccountService {
    /**
     * Process
     * given transaction - this is to be called by the credit and debit generation threads.
     *
     * @param transaction transaction to process
     */
    void processTransaction(Transaction transaction);

    /**
     * Retrieve the balance in the account
     */
    double retrieveBalance();
}