package org.example.transactionservice.client;

import java.util.UUID;

public record BalanceResponse(UUID accountId, double balance) {
}
