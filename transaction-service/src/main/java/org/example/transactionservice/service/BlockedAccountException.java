package org.example.transactionservice.service;

import java.util.UUID;

public class BlockedAccountException extends RuntimeException {
    public BlockedAccountException(UUID id) {
        super("Account is blocked: " + id);
    }
}
