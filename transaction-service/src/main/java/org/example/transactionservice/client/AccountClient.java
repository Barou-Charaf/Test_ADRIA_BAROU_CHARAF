package org.example.transactionservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(
        name = "account-servie",
        configuration = AccountClientConfiguration.class)
public interface AccountClient {

    @GetMapping("/api/accounts/{id}")
    AccountResponse getAccount(@PathVariable("id") UUID id);

    @GetMapping("/api/accounts/{id}/balance")
    BalanceResponse getBalance(@PathVariable("id") UUID id);

    @PostMapping("/api/accounts/{id}/debit")
    AccountResponse debit(@PathVariable("id") UUID id, @RequestBody AmountRequest request);

    @PostMapping("/api/accounts/{id}/credit")
    AccountResponse credit(@PathVariable("id") UUID id, @RequestBody AmountRequest request);
}
