package org.example.accountservie.web.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.accountservie.domain.Account;
import org.example.accountservie.service.AccountService;
import org.example.accountservie.web.rest.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountRestController {

    private final AccountService accountService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse createAccount(@Valid @RequestBody CreateAccountRequest request) {
        Account account = accountService.createAccount(
                request.customerReference(), request.status(), request.currency(), request.balance());
        return AccountResponse.from(account);
    }

    @GetMapping
    public List<AccountResponse> getAllAccounts() {
        return accountService.getAllAccounts().stream().map(AccountResponse::from).toList();
    }

    @GetMapping("/{id}")
    public AccountResponse getAccount(@PathVariable UUID id) {
        return AccountResponse.from(accountService.getAccount(id));
    }

    @GetMapping("/{id}/balance")
    public BalanceResponse getBalance(@PathVariable UUID id) {
        return new BalanceResponse(id, accountService.getBalance(id));
    }

    @PatchMapping("/{id}/status")
    public AccountResponse updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAccountStatusRequest request) {
        return AccountResponse.from(accountService.updateStatus(id, request.status()));
    }

    @PostMapping("/{id}/debit")
    public AccountResponse debit(@PathVariable UUID id, @Valid @RequestBody AmountRequest request) {
        return AccountResponse.from(accountService.debit(id, request.amount()));
    }

    @PostMapping("/{id}/credit")
    public AccountResponse credit(@PathVariable UUID id, @Valid @RequestBody AmountRequest request) {
        return AccountResponse.from(accountService.credit(id, request.amount()));
    }
}
