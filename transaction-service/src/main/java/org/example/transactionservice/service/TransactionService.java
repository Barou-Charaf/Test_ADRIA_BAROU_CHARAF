package org.example.transactionservice.service;

import org.example.transactionservice.domain.Transaction;
import org.example.transactionservice.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final BigDecimal maximumAmount;

    public TransactionService(
            TransactionRepository transactionRepository,
            @Value("${bank.transaction.max-amount}") BigDecimal maximumAmount) {
        this.transactionRepository = transactionRepository;
        this.maximumAmount = maximumAmount;
    }

    public Transaction createTransaction(
            UUID accountId,
            BigDecimal amount,
            String currency,
            String merchantCategory,
            String country) {
        validateTransactionData(accountId, amount, currency, merchantCategory, country);

        // Persistence must wait for C3: the account must first be verified as
        // existing, ACTIVE, sufficiently funded, and using the same currency.
        throw new AccountIntegrationRequiredException();
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public Transaction getTransaction(UUID id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));
    }

    public List<Transaction> getTransactionsByAccount(UUID accountId) {
        if (accountId == null) {
            throw new InvalidTransactionException("Account id is required");
        }
        return transactionRepository.findByAccountId(accountId);
    }

    public void transfer(UUID sourceAccountId, UUID destinationAccountId, BigDecimal amount, String currency) {
        if (sourceAccountId == null) {
            throw new InvalidTransactionException("Source account id is required");
        }
        if (destinationAccountId == null) {
            throw new InvalidTransactionException("Destination account id is required");
        }
        validateAmount(amount);
        requireText(currency, "Currency");

        // C3 must verify both accounts and coordinate debit/credit without
        // falsely claiming a distributed transaction succeeded.
        throw new AccountIntegrationRequiredException();
    }

    private void validateTransactionData(
            UUID accountId,
            BigDecimal amount,
            String currency,
            String merchantCategory,
            String country) {
        if (accountId == null) {
            throw new InvalidTransactionException("Account id is required");
        }
        validateAmount(amount);
        requireText(currency, "Currency");
        requireText(merchantCategory, "Merchant category");
        requireText(country, "Country");
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new InvalidTransactionException("Amount is required");
        }
        if (amount.signum() <= 0) {
            throw new InvalidTransactionException("Amount must be strictly positive");
        }
        if (amount.compareTo(maximumAmount) > 0) {
            throw new InvalidTransactionException("Amount must not exceed " + maximumAmount);
        }
    }

    private void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new InvalidTransactionException(fieldName + " is required");
        }
    }
}
