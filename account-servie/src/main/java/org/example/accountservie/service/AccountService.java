package org.example.accountservie.service;

import org.example.accountservie.domain.Account;
import org.example.accountservie.domain.AccountStatus;
import org.example.accountservie.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AccountService {

    private final AccountRepository accountRepository;
    private final double maximumTransactionAmount;

    public AccountService(
            AccountRepository accountRepository,
            @Value("${bank.transaction.max-amount}") double maximumTransactionAmount) {
        this.accountRepository = accountRepository;
        this.maximumTransactionAmount = maximumTransactionAmount;
    }

    @Transactional
    public Account createAccount(String customerReference, AccountStatus status, String currency, double balance) {
        validateAccountData(customerReference, status, currency);
        validateInitialBalance(balance);
        Account account = Account.builder()
                .customerReference(customerReference)
                .status(status)
                .currency(currency)
                .balance(balance)
                .build();
        return accountRepository.save(account);
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Account getAccount(UUID id) {
        return accountRepository.findById(id).orElseThrow(() -> new AccountNotFoundException(id));
    }

    public double getBalance(UUID id) {
        return getAccount(id).getBalance();
    }

    @Transactional
    public Account updateStatus(UUID id, AccountStatus status) {
        if (status == null) {
            throw new InvalidAccountDataException("Account status is required");
        }
        Account account = getAccount(id);
        account.setStatus(status);
        return accountRepository.save(account);
    }

    @Transactional
    public Account debit(UUID id, double amount) {
        validateOperationAmount(amount);
        Account account = getActiveAccount(id);
        if (account.getBalance() < amount) {
            throw new InsufficientBalanceException();
        }
        account.setBalance(account.getBalance() - amount);
        return accountRepository.save(account);
    }

    @Transactional
    public Account credit(UUID id, double amount) {
        validateOperationAmount(amount);
        Account account = getActiveAccount(id);
        account.setBalance(account.getBalance() + amount);
        return accountRepository.save(account);
    }

    private Account getActiveAccount(UUID id) {
        Account account = getAccount(id);
        if (account.getStatus() == AccountStatus.BLOCKED) {
            throw new AccountBlockedException(id);
        }
        return account;
    }

    private void validateInitialBalance(double balance) {
        if (!Double.isFinite(balance) || balance < 0) {
            throw new InvalidAmountException("Initial balance must be a finite, non-negative amount");
        }
    }

    private void validateOperationAmount(double amount) {
        if (!Double.isFinite(amount) || amount <= 0) {
            throw new InvalidAmountException("Amount must be finite and strictly positive");
        }
        if (amount > maximumTransactionAmount) {
            throw new InvalidAmountException(
                    "Amount must not exceed " + maximumTransactionAmount);
        }
    }

    private void validateAccountData(String customerReference, AccountStatus status, String currency) {
        if (customerReference == null || customerReference.isBlank()) {
            throw new InvalidAccountDataException("Customer reference is required");
        }
        if (status == null) {
            throw new InvalidAccountDataException("Account status is required");
        }
        if (currency == null || currency.isBlank()) {
            throw new InvalidAccountDataException("Account currency is required");
        }
    }
}
