package org.example.transactionservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.example.transactionservice.service.AccountServiceUnavailableException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class AccountGateway {

    private final AccountClient accountClient;

    public AccountGateway(AccountClient accountClient) {
        this.accountClient = accountClient;
    }

    @Retry(name = "accountServiceRead", fallbackMethod = "accountFallback")
    @CircuitBreaker(name = "accountServiceRead")
    public AccountResponse getAccount(UUID id) {
        return accountClient.getAccount(id);
    }

    @Retry(name = "accountServiceRead", fallbackMethod = "balanceFallback")
    @CircuitBreaker(name = "accountServiceRead")
    public BalanceResponse getBalance(UUID id) {
        return accountClient.getBalance(id);
    }

    @CircuitBreaker(name = "accountServiceWrite", fallbackMethod = "writeFallback")
    public AccountResponse debit(UUID id, BigDecimal amount) {
        return accountClient.debit(id, new AmountRequest(amount.doubleValue()));
    }

    @CircuitBreaker(name = "accountServiceWrite", fallbackMethod = "writeFallback")
    public AccountResponse credit(UUID id, BigDecimal amount) {
        return accountClient.credit(id, new AmountRequest(amount.doubleValue()));
    }

    private AccountResponse accountFallback(UUID id, Throwable failure) {
        throw translate(failure);
    }

    private BalanceResponse balanceFallback(UUID id, Throwable failure) {
        throw translate(failure);
    }

    private AccountResponse writeFallback(UUID id, BigDecimal amount, Throwable failure) {
        throw translate(failure);
    }

    private RuntimeException translate(Throwable failure) {
        if (failure instanceof RemoteAccountNotFoundException notFound) {
            return notFound;
        }
        if (failure instanceof RemoteAccountOperationException rejected) {
            return rejected;
        }
        return new AccountServiceUnavailableException(failure);
    }
}
