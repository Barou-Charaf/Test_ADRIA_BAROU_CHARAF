package org.example.transactionservice.service;

public class AccountServiceUnavailableException extends RuntimeException {
    public AccountServiceUnavailableException(Throwable cause) {
        super("Account service is unavailable; the banking operation was not reported as successful", cause);
    }
}
