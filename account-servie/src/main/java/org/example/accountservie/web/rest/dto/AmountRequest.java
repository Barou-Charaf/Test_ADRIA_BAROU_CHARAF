package org.example.accountservie.web.rest.dto;

import jakarta.validation.constraints.DecimalMin;

public record AmountRequest(@DecimalMin(value = "0.0", inclusive = false) double amount) {
}
