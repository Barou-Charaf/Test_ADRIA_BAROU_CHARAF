package org.example.accountservie.web.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.accountservie.domain.AccountStatus;

public record CreateAccountRequest(
        @NotBlank String customerReference,
        @NotNull AccountStatus status,
        @NotBlank String currency,
        @DecimalMin(value = "0.0", inclusive = true) double balance
) {
}
