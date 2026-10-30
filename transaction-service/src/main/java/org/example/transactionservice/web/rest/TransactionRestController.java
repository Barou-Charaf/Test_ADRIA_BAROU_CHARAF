package org.example.transactionservice.web.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.transactionservice.service.TransactionService;
import org.example.transactionservice.web.rest.dto.CreateTransactionRequest;
import org.example.transactionservice.web.rest.dto.TransactionResponse;
import org.example.transactionservice.web.rest.dto.TransferRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionRestController {

    private final TransactionService transactionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse createTransaction(@Valid @RequestBody CreateTransactionRequest request) {
        return TransactionResponse.from(transactionService.createTransaction(
                request.accountId(),
                request.amount(),
                request.currency(),
                request.merchantCategory(),
                request.country()
        ));
    }

    @GetMapping
    public List<TransactionResponse> getAllTransactions() {
        return transactionService.getAllTransactions().stream().map(TransactionResponse::from).toList();
    }

    @GetMapping("/{id}")
    public TransactionResponse getTransaction(@PathVariable UUID id) {
        return TransactionResponse.from(transactionService.getTransaction(id));
    }

    @GetMapping("/account/{accountId}")
    public List<TransactionResponse> getTransactionsByAccount(@PathVariable UUID accountId) {
        return transactionService.getTransactionsByAccount(accountId).stream()
                .map(TransactionResponse::from)
                .toList();
    }

    @PostMapping("/transfer")
    @ResponseStatus(HttpStatus.CREATED)
    public void transfer(@Valid @RequestBody TransferRequest request) {
        transactionService.transfer(
                request.sourceAccountId(),
                request.destinationAccountId(),
                request.amount(),
                request.currency()
        );
    }
}
