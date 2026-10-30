package org.example.transactionservice.service;

public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException() {
        super("Insufficient account balance");
    }
}
