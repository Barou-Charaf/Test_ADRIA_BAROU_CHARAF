package org.example.transactionservice.messaging;

import org.example.transactionservice.domain.Transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionCreatedEvent(
        UUID id,
        UUID accountId,
        BigDecimal amount,
        String currency,
        String merchantCategory,
        String country,
        Instant createdAt) {

    public static TransactionCreatedEvent from(Transaction transaction) {
        return new TransactionCreatedEvent(
                transaction.getId(),
                transaction.getAccountId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getMerchantCategory(),
                transaction.getCountry(),
                transaction.getCreatedAt());
    }
}
