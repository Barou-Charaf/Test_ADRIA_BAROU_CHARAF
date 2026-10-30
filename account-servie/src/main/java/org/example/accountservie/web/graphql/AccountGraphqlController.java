package org.example.accountservie.web.graphql;

import lombok.RequiredArgsConstructor;
import org.example.accountservie.domain.AccountStatus;
import org.example.accountservie.service.AccountService;
import org.example.accountservie.web.rest.dto.AccountResponse;
import org.example.accountservie.web.rest.dto.BalanceResponse;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class AccountGraphqlController {

    private final AccountService accountService;

    @QueryMapping
    public List<AccountResponse> accounts() {
        return accountService.getAllAccounts().stream().map(AccountResponse::from).toList();
    }

    @QueryMapping
    public AccountResponse account(@Argument UUID id) {
        return AccountResponse.from(accountService.getAccount(id));
    }

    @QueryMapping
    public BalanceResponse accountBalance(@Argument UUID id) {
        return new BalanceResponse(id, accountService.getBalance(id));
    }

    @MutationMapping
    public AccountResponse createAccount(@Argument CreateAccountInput input) {
        return AccountResponse.from(accountService.createAccount(
                input.customerReference(), input.status(), input.currency(), input.balance()));
    }

    @MutationMapping
    public AccountResponse updateAccountStatus(@Argument UUID id, @Argument AccountStatus status) {
        return AccountResponse.from(accountService.updateStatus(id, status));
    }

    @MutationMapping
    public AccountResponse debitAccount(@Argument UUID id, @Argument double amount) {
        return AccountResponse.from(accountService.debit(id, amount));
    }

    @MutationMapping
    public AccountResponse creditAccount(@Argument UUID id, @Argument double amount) {
        return AccountResponse.from(accountService.credit(id, amount));
    }

    public record CreateAccountInput(
            String customerReference,
            AccountStatus status,
            String currency,
            double balance
    ) {
    }
}
