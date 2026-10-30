package org.example.transactionservice.client;

public class RemoteAccountNotFoundException extends RuntimeException {
    public RemoteAccountNotFoundException() {
        super("Account not found");
    }
}
