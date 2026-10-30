package org.example.accountservie.web.rest.dto;

import java.util.UUID;

public record BalanceResponse(UUID accountId, double balance) {
}
