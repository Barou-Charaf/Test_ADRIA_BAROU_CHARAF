package org.example.transactionservice.client;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class AccountClientConfiguration {

    @Bean
    ErrorDecoder accountErrorDecoder() {
        return (methodKey, response) -> switch (response.status()) {
            case 404 -> new RemoteAccountNotFoundException();
            case 400 -> new RemoteAccountOperationException("Account service rejected an invalid request");
            case 409 -> new RemoteAccountOperationException("Account service rejected the balance operation");
            default -> new ErrorDecoder.Default().decode(methodKey, response);
        };
    }
}
