package org.example.transactionservice.service;

public class TransactionProcessingException extends RuntimeException {
    public TransactionProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
