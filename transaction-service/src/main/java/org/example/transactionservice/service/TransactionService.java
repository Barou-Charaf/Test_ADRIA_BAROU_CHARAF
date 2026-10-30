package org.example.transactionservice.service;

import org.example.transactionservice.client.AccountGateway;
import org.example.transactionservice.client.AccountResponse;
import org.example.transactionservice.client.AccountStatus;
import org.example.transactionservice.client.RemoteAccountNotFoundException;
import org.example.transactionservice.domain.Transaction;
import org.example.transactionservice.messaging.TransactionCreatedEvent;
import org.example.transactionservice.repository.TransactionRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountGateway accountGateway;
    private final BigDecimal maximumAmount;
    private final ApplicationEventPublisher eventPublisher;

    public TransactionService(
            TransactionRepository transactionRepository,
            AccountGateway accountGateway,
            @Value("${bank.transaction.max-amount}") BigDecimal maximumAmount,
            ApplicationEventPublisher eventPublisher) {
        this.transactionRepository = transactionRepository;
        this.accountGateway = accountGateway;
        this.maximumAmount = maximumAmount;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Transaction createTransaction(
            UUID accountId,
            BigDecimal amount,
            String currency,
            String merchantCategory,
            String country) {
        validateTransactionData(accountId, amount, currency, merchantCategory, country);
        AccountResponse account = getRequiredAccount(accountId);
        validateActive(account);
        validateCurrency(currency, account);
        validateBalance(accountId, amount);

        accountGateway.debit(accountId, amount);
        try {
            Transaction transaction = buildTransaction(
                    accountId, amount, currency, merchantCategory, country, Instant.now());
            Transaction savedTransaction = transactionRepository.saveAndFlush(transaction);
            eventPublisher.publishEvent(TransactionCreatedEvent.from(savedTransaction));
            return savedTransaction;
        } catch (RuntimeException persistenceFailure) {
            compensateCredit(accountId, amount, persistenceFailure,
                    "Transaction persistence failed and the account debit could not be compensated");
            throw new TransactionProcessingException(
                    "Transaction persistence failed; the account debit was compensated", persistenceFailure);
        }
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

    @Transactional
    public List<Transaction> transfer(
            UUID sourceAccountId,
            UUID destinationAccountId,
            BigDecimal amount,
            String currency,
            String merchantCategory,
            String country) {
        if (sourceAccountId == null) {
            throw new InvalidTransactionException("Source account id is required");
        }
        if (destinationAccountId == null) {
            throw new InvalidTransactionException("Destination account id is required");
        }
        validateAmount(amount);
        requireText(currency, "Currency");
        requireText(merchantCategory, "Merchant category");
        requireText(country, "Country");

        AccountResponse source = getRequiredAccount(sourceAccountId);
        AccountResponse destination = getRequiredAccount(destinationAccountId);
        validateActive(source);
        validateActive(destination);
        validateCurrency(currency, source);
        validateCurrency(currency, destination);
        validateBalance(sourceAccountId, amount);

        accountGateway.debit(sourceAccountId, amount);
        try {
            accountGateway.credit(destinationAccountId, amount);
        } catch (RuntimeException creditFailure) {
            compensateCredit(sourceAccountId, amount, creditFailure,
                    "Destination credit failed and the source debit could not be compensated");
            throw new TransactionProcessingException(
                    "Destination credit failed; the source debit was compensated", creditFailure);
        }

        try {
            Instant createdAt = Instant.now();
            Transaction sourceTransaction = buildTransaction(
                    sourceAccountId, amount, currency, merchantCategory, country, createdAt);
            Transaction destinationTransaction = buildTransaction(
                    destinationAccountId, amount, currency, merchantCategory, country, createdAt);
            List<Transaction> savedTransactions = transactionRepository.saveAllAndFlush(
                    List.of(sourceTransaction, destinationTransaction));
            savedTransactions.stream()
                    .map(TransactionCreatedEvent::from)
                    .forEach(eventPublisher::publishEvent);
            return savedTransactions;
        } catch (RuntimeException persistenceFailure) {
            compensateTransfer(sourceAccountId, destinationAccountId, amount, persistenceFailure);
            throw new TransactionProcessingException(
                    "Transfer persistence failed; both balance changes were compensated", persistenceFailure);
        }
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

    private AccountResponse getRequiredAccount(UUID accountId) {
        try {
            return accountGateway.getAccount(accountId);
        } catch (RemoteAccountNotFoundException exception) {
            throw new AccountNotFoundException(accountId);
        }
    }

    private void validateActive(AccountResponse account) {
        if (account.status() != AccountStatus.ACTIVE) {
            throw new BlockedAccountException(account.id());
        }
    }

    private void validateCurrency(String currency, AccountResponse account) {
        if (!currency.equals(account.currency())) {
            throw new CurrencyMismatchException();
        }
    }

    private void validateBalance(UUID accountId, BigDecimal amount) {
        double currentBalance = accountGateway.getBalance(accountId).balance();
        if (BigDecimal.valueOf(currentBalance).compareTo(amount) < 0) {
            throw new InsufficientBalanceException();
        }
    }

    private Transaction buildTransaction(
            UUID accountId,
            BigDecimal amount,
            String currency,
            String merchantCategory,
            String country,
            Instant createdAt) {
        return Transaction.builder()
                .accountId(accountId)
                .amount(amount)
                .currency(currency)
                .merchantCategory(merchantCategory)
                .country(country)
                .createdAt(createdAt)
                .build();
    }

    private void compensateCredit(
            UUID accountId,
            BigDecimal amount,
            Throwable originalFailure,
            String failureMessage) {
        try {
            accountGateway.credit(accountId, amount);
        } catch (RuntimeException compensationFailure) {
            throw new CompensationFailedException(failureMessage, originalFailure, compensationFailure);
        }
    }

    private void compensateTransfer(
            UUID sourceAccountId,
            UUID destinationAccountId,
            BigDecimal amount,
            Throwable originalFailure) {
        try {
            accountGateway.debit(destinationAccountId, amount);
            accountGateway.credit(sourceAccountId, amount);
        } catch (RuntimeException compensationFailure) {
            throw new CompensationFailedException(
                    "Transfer persistence failed and its balance changes could not be fully compensated",
                    originalFailure,
                    compensationFailure);
        }
    }
}
