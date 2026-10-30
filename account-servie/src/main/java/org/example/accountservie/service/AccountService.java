package org.example.accountservie.service;

import lombok.RequiredArgsConstructor;
import org.example.accountservie.domain.Account;
import org.example.accountservie.domain.AccountStatus;
import org.example.accountservie.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountService {

    private final AccountRepository accountRepository;

    @Transactional
    public Account createAccount(String customerReference, AccountStatus status, String currency, double balance) {
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
    }
}
