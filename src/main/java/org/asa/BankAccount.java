package org.asa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class BankAccount {
    public static void main(String[] args) {
        SpringApplication.run(BankAccount.class, args);
        System.out.println("Bank App Started Successfully");
    }
}