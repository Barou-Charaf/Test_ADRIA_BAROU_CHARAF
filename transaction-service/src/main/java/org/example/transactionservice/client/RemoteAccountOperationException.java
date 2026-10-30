package org.example.transactionservice.client;

public class RemoteAccountOperationException extends RuntimeException {
    public RemoteAccountOperationException(String message) {
        super(message);
    }
}
