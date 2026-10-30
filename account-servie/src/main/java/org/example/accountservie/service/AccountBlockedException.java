package org.example.accountservie.service;

import java.util.UUID;

public class AccountBlockedException extends RuntimeException {
    public AccountBlockedException(UUID id) {
        super("Account is blocked: " + id);
    }
}
