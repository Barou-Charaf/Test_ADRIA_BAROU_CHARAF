package org.example.accountservie.web.rest.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AmountRequest(@NotNull @Positive Double amount) {
}
