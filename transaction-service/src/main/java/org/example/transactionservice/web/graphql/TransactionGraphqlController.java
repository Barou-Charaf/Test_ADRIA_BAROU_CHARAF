package org.example.transactionservice.web.graphql;

import lombok.RequiredArgsConstructor;
import org.example.transactionservice.service.TransactionService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class TransactionGraphqlController {

    private final TransactionService transactionService;

    @QueryMapping
    public List<TransactionGraphqlResponse> transactions() {
        return transactionService.getAllTransactions().stream().map(TransactionGraphqlResponse::from).toList();
    }

    @QueryMapping
    public TransactionGraphqlResponse transaction(@Argument UUID id) {
        return TransactionGraphqlResponse.from(transactionService.getTransaction(id));
    }

    @QueryMapping
    public List<TransactionGraphqlResponse> transactionsByAccount(@Argument UUID accountId) {
        return transactionService.getTransactionsByAccount(accountId).stream()
                .map(TransactionGraphqlResponse::from)
                .toList();
    }

    @MutationMapping
    public TransactionGraphqlResponse createTransaction(@Argument CreateTransactionInput input) {
        return TransactionGraphqlResponse.from(transactionService.createTransaction(
                input.accountId(),
                input.amount(),
                input.currency(),
                input.merchantCategory(),
                input.country()
        ));
    }

    @MutationMapping
    public boolean transfer(@Argument TransferInput input) {
        transactionService.transfer(
                input.sourceAccountId(),
                input.destinationAccountId(),
                input.amount(),
                input.currency()
        );
        return true;
    }

    public record CreateTransactionInput(
            UUID accountId,
            BigDecimal amount,
            String currency,
            String merchantCategory,
            String country
    ) {
    }

    public record TransferInput(
            UUID sourceAccountId,
            UUID destinationAccountId,
            BigDecimal amount,
            String currency
    ) {
    }
}
