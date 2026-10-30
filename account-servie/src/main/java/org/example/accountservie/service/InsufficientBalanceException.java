package org.example.accountservie.service;

public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException() {
        super("Insufficient account balance");
    }
}
