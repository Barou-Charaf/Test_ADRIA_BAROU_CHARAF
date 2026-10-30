package org.example.accountservie.mcp;

import org.example.accountservie.domain.Account;
import org.example.accountservie.service.AccountService;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class AccountMcpTools {

    private final AccountService accountService;

    public AccountMcpTools(AccountService accountService) {
        this.accountService = accountService;
    }

    @McpTool(description = "List all bank accounts")
    public List<Account> listAccounts() {
        return accountService.getAllAccounts();
    }

    @McpTool(description = "Consult a bank account by its UUID")
    public Account getAccount(
            @McpToolParam(description = "Account UUID", required = true) UUID accountId) {
        return accountService.getAccount(accountId);
    }

    @McpTool(description = "Consult the current balance of a bank account")
    public AccountBalance getAccountBalance(
            @McpToolParam(description = "Account UUID", required = true) UUID accountId) {
        return new AccountBalance(accountId, accountService.getBalance(accountId));
    }

    public record AccountBalance(UUID accountId, double balance) {
    }
}
