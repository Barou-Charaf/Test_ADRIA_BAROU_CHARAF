package org.example.transactionservice.service;

public class CompensationFailedException extends RuntimeException {
    public CompensationFailedException(String message, Throwable originalFailure, Throwable compensationFailure) {
        super(message, compensationFailure);
        addSuppressed(originalFailure);
    }
}
