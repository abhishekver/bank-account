package org.asa.controller;

import org.asa.models.Transaction;
import org.asa.services.BankAccountService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class BankAccountController {

    private final BankAccountService bankAccountService;

    public BankAccountController(final BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    @GetMapping("/balance")
    public Double getBalance() {
        return bankAccountService.retrieveBalance();
    }

    @PostMapping("/transaction")
    public void transaction(@RequestParam double amount) {
        Transaction transaction = new Transaction(UUID.randomUUID().toString(), amount);
        bankAccountService.processTransaction(transaction);
    }
}
