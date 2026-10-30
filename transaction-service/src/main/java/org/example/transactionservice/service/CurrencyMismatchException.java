package org.example.transactionservice.service;

public class CurrencyMismatchException extends RuntimeException {
    public CurrencyMismatchException() {
        super("Transaction currency does not match the account currency");
    }
}
