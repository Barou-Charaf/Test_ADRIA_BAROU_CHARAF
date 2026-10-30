package org.example.accountservie.web.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.example.accountservie.domain.AccountStatus;

public record CreateAccountRequest(
        @NotBlank String customerReference,
        @NotNull AccountStatus status,
        @NotBlank String currency,
        @PositiveOrZero double balance
) {
}
