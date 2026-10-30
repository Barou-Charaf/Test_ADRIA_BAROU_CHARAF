package org.example.transactionservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountGatewayTest {

    @Test
    void delegatesSuccessfulAccountConsultationToFeignClient() {
        AccountClient client = mock(AccountClient.class);
        AccountGateway gateway = new AccountGateway(client);
        UUID accountId = UUID.randomUUID();
        AccountResponse expected = new AccountResponse(
                accountId, "CUSTOMER", AccountStatus.ACTIVE, "MAD", 500);
        when(client.getAccount(accountId)).thenReturn(expected);

        assertThat(gateway.getAccount(accountId)).isEqualTo(expected);
        verify(client).getAccount(accountId);
    }

    @Test
    void readsUseRetryAndCircuitBreaker() throws NoSuchMethodException {
        Method method = AccountGateway.class.getMethod("getAccount", UUID.class);
        assertThat(method.getAnnotation(Retry.class)).isNotNull();
        assertThat(method.getAnnotation(CircuitBreaker.class)).isNotNull();
    }

    @Test
    void writesUseCircuitBreakerWithoutRetry() throws NoSuchMethodException {
        Method debit = AccountGateway.class.getMethod("debit", UUID.class, BigDecimal.class);
        Method credit = AccountGateway.class.getMethod("credit", UUID.class, BigDecimal.class);

        assertThat(debit.getAnnotation(CircuitBreaker.class)).isNotNull();
        assertThat(credit.getAnnotation(CircuitBreaker.class)).isNotNull();
        assertThat(debit.getAnnotation(Retry.class)).isNull();
        assertThat(credit.getAnnotation(Retry.class)).isNull();
    }
}
