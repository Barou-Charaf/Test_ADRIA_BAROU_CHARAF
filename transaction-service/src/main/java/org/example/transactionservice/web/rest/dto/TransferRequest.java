package org.example.transactionservice.web.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(
        @NotNull UUID sourceAccountId,
        @NotNull UUID destinationAccountId,
        @NotNull @Positive BigDecimal amount,
        @NotBlank String currency,
        @NotBlank String merchantCategory,
        @NotBlank String country
) {
}
