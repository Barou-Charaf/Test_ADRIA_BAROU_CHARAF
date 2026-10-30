package org.example.chatbotservice.web;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(@NotBlank String message) {
}
