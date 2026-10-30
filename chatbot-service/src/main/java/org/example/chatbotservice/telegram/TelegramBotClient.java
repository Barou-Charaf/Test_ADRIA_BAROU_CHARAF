package org.example.chatbotservice.telegram;

import org.example.chatbotservice.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class TelegramBotClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramBotClient.class);

    private final ChatService chatService;
    private final RestClient restClient;
    private final boolean enabled;
    private long nextUpdateOffset;

    public TelegramBotClient(
            ChatService chatService,
            RestClient.Builder restClientBuilder,
            @Value("${telegram.bot.token:}") String botToken) {
        this.chatService = chatService;
        this.enabled = botToken != null && !botToken.isBlank();
        this.restClient = enabled
                ? restClientBuilder.baseUrl("https://api.telegram.org/bot" + botToken).build()
                : restClientBuilder.build();
    }

    @Scheduled(fixedDelayString = "${telegram.bot.poll-delay:1000}")
    public void pollUpdates() {
        if (!enabled) {
            return;
        }

        try {
            TelegramResponse<List<TelegramUpdate>> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/getUpdates")
                            .queryParam("offset", nextUpdateOffset)
                            .queryParam("timeout", 20)
                            .queryParam("allowed_updates", "[\"message\"]")
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

            if (response == null || !response.ok() || response.result() == null) {
                LOGGER.warn("Telegram getUpdates returned an unsuccessful response");
                return;
            }

            for (TelegramUpdate update : response.result()) {
                handleUpdate(update);
                nextUpdateOffset = Math.max(nextUpdateOffset, update.update_id() + 1L);
            }
        } catch (RuntimeException exception) {
            LOGGER.warn("Unable to poll Telegram updates");
        }
    }

    private void handleUpdate(TelegramUpdate update) {
        TelegramMessage message = update.message();
        if (message == null || message.chat() == null || message.text() == null || message.text().isBlank()) {
            return;
        }

        String response;
        try {
            response = chatService.chat(message.text());
        } catch (RuntimeException exception) {
            LOGGER.warn("Unable to process Telegram chat request: {}", exception.getClass().getSimpleName());
            response = "The request could not be completed. Please verify that the banking services are available and try again.";
        }

        sendMessage(message.chat().id(), response);
    }

    private void sendMessage(long chatId, String text) {
        String message = text == null || text.isBlank() ? "No response was generated." : text;
        for (int start = 0; start < message.length(); start += 4096) {
            String chunk = message.substring(start, Math.min(start + 4096, message.length()));
            restClient.post()
                    .uri("/sendMessage")
                    .body(new SendMessageRequest(chatId, chunk))
                    .retrieve()
                    .toBodilessEntity();
        }
    }

    private record TelegramResponse<T>(boolean ok, T result) {
    }

    private record TelegramUpdate(long update_id, TelegramMessage message) {
    }

    private record TelegramMessage(TelegramChat chat, String text) {
    }

    private record TelegramChat(long id) {
    }

    private record SendMessageRequest(long chat_id, String text) {
    }
}
