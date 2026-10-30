package org.example.transactionservice.web.graphql;

import org.example.transactionservice.domain.Transaction;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

public record TransactionGraphqlResponse(
        UUID id,
        UUID accountId,
        BigDecimal amount,
        String currency,
        String merchantCategory,
        String country,
        OffsetDateTime createdAt
) {
    public static TransactionGraphqlResponse from(Transaction transaction) {
        return new TransactionGraphqlResponse(
                transaction.getId(),
                transaction.getAccountId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getMerchantCategory(),
                transaction.getCountry(),
                transaction.getCreatedAt().atOffset(ZoneOffset.UTC)
        );
    }
}
