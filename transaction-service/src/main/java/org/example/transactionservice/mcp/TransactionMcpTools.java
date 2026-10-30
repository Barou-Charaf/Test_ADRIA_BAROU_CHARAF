package org.example.transactionservice.mcp;

import org.example.transactionservice.domain.Transaction;
import org.example.transactionservice.service.TransactionService;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
public class TransactionMcpTools {

    private final TransactionService transactionService;

    public TransactionMcpTools(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @McpTool(description = "List the transactions recorded for a bank account")
    public List<Transaction> listTransactionsByAccount(
            @McpToolParam(description = "Account UUID", required = true) UUID accountId) {
        return transactionService.getTransactionsByAccount(accountId);
    }

    @McpTool(description = "Create a card transaction after validating the account, currency and balance")
    public Transaction createTransaction(
            @McpToolParam(description = "Account UUID", required = true) UUID accountId,
            @McpToolParam(description = "Strictly positive amount, maximum 1000", required = true) BigDecimal amount,
            @McpToolParam(description = "Transaction currency matching the account", required = true) String currency,
            @McpToolParam(description = "Merchant category", required = true) String merchantCategory,
            @McpToolParam(description = "Transaction country", required = true) String country) {
        return transactionService.createTransaction(accountId, amount, currency, merchantCategory, country);
    }

    @McpTool(description = "Transfer money between two active accounts after validating currency and balance")
    public List<Transaction> transfer(
            @McpToolParam(description = "Source account UUID", required = true) UUID sourceAccountId,
            @McpToolParam(description = "Destination account UUID", required = true) UUID destinationAccountId,
            @McpToolParam(description = "Strictly positive amount, maximum 1000", required = true) BigDecimal amount,
            @McpToolParam(description = "Transfer currency matching both accounts", required = true) String currency,
            @McpToolParam(description = "Merchant category", required = true) String merchantCategory,
            @McpToolParam(description = "Transfer country", required = true) String country) {
        return transactionService.transfer(
                sourceAccountId, destinationAccountId, amount, currency, merchantCategory, country);
    }
}
