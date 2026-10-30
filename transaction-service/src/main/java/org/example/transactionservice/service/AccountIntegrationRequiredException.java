package org.example.transactionservice.service;

public class AccountIntegrationRequiredException extends RuntimeException {
    public AccountIntegrationRequiredException() {
        super("Account verification is unavailable until Step C3 account-service integration is implemented");
    }
}
