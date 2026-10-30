package org.example.accountservie.web.rest.dto;

import jakarta.validation.constraints.NotNull;
import org.example.accountservie.domain.AccountStatus;

public record UpdateAccountStatusRequest(@NotNull AccountStatus status) {
}
