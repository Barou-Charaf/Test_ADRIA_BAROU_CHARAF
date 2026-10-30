package org.example.transactionservice.service;

import org.example.transactionservice.client.*;
import org.example.transactionservice.domain.Transaction;
import org.example.transactionservice.repository.TransactionRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountGateway accountGateway;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(
                transactionRepository, accountGateway, new BigDecimal("1000"), eventPublisher);
    }

    @Test
    void createsTransactionAfterAccountValidationAndDebit() {
        UUID accountId = UUID.randomUUID();
        mockActiveAccount(accountId, "MAD", 500);
        when(transactionRepository.saveAndFlush(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = transactionService.createTransaction(
                accountId, new BigDecimal("100"), "MAD", "RETAIL", "MA");

        assertThat(result.getAccountId()).isEqualTo(accountId);
        assertThat(result.getCreatedAt()).isNotNull();
        verify(accountGateway).debit(accountId, new BigDecimal("100"));
    }

    @Test
    void rejectsNonPositiveAmount() {
        assertThatThrownBy(() -> create(UUID.randomUUID(), BigDecimal.ZERO, "MAD"))
                .isInstanceOf(InvalidTransactionException.class);
        verifyNoInteractions(accountGateway, transactionRepository);
    }

    @Test
    void rejectsAmountAboveConfiguredMaximum() {
        assertThatThrownBy(() -> create(UUID.randomUUID(), new BigDecimal("1000.01"), "MAD"))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("1000");
    }

    @Test
    void rejectsUnknownAccount() {
        UUID accountId = UUID.randomUUID();
        when(accountGateway.getAccount(accountId)).thenThrow(new RemoteAccountNotFoundException());

        assertThatThrownBy(() -> create(accountId, BigDecimal.TEN, "MAD"))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void rejectsBlockedAccount() {
        UUID accountId = UUID.randomUUID();
        when(accountGateway.getAccount(accountId))
                .thenReturn(account(accountId, AccountStatus.BLOCKED, "MAD", 500));

        assertThatThrownBy(() -> create(accountId, BigDecimal.TEN, "MAD"))
                .isInstanceOf(BlockedAccountException.class);
        verify(accountGateway, never()).debit(any(), any());
    }

    @Test
    void rejectsCurrencyMismatch() {
        UUID accountId = UUID.randomUUID();
        when(accountGateway.getAccount(accountId))
                .thenReturn(account(accountId, AccountStatus.ACTIVE, "EUR", 500));

        assertThatThrownBy(() -> create(accountId, BigDecimal.TEN, "MAD"))
                .isInstanceOf(CurrencyMismatchException.class);
    }

    @Test
    void rejectsInsufficientBalance() {
        UUID accountId = UUID.randomUUID();
        mockActiveAccount(accountId, "MAD", 5);

        assertThatThrownBy(() -> create(accountId, BigDecimal.TEN, "MAD"))
                .isInstanceOf(InsufficientBalanceException.class);
        verify(accountGateway, never()).debit(any(), any());
    }

    @Test
    void doesNotReportSuccessWhenAccountServiceIsUnavailable() {
        UUID accountId = UUID.randomUUID();
        when(accountGateway.getAccount(accountId))
                .thenThrow(new AccountServiceUnavailableException(new RuntimeException("offline")));

        assertThatThrownBy(() -> create(accountId, BigDecimal.TEN, "MAD"))
                .isInstanceOf(AccountServiceUnavailableException.class);
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void compensatesDebitWhenPersistenceFails() {
        UUID accountId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("100");
        mockActiveAccount(accountId, "MAD", 500);
        when(transactionRepository.saveAndFlush(any(Transaction.class)))
                .thenThrow(new RuntimeException("database failure"));

        assertThatThrownBy(() -> create(accountId, amount, "MAD"))
                .isInstanceOf(TransactionProcessingException.class)
                .hasMessageContaining("compensated");
        verify(accountGateway).credit(accountId, amount);
    }

    @Test
    void transfersBetweenValidatedAccounts() {
        UUID sourceId = UUID.randomUUID();
        UUID destinationId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("100");
        mockActiveAccount(sourceId, "MAD", 500);
        when(accountGateway.getAccount(destinationId))
                .thenReturn(account(destinationId, AccountStatus.ACTIVE, "MAD", 200));
        when(transactionRepository.saveAllAndFlush(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<Transaction> result = transactionService.transfer(
                sourceId, destinationId, amount, "MAD", "TRANSFER", "MA");

        assertThat(result).hasSize(2);
        verify(accountGateway).debit(sourceId, amount);
        verify(accountGateway).credit(destinationId, amount);
    }

    @Test
    void compensatesSourceWhenDestinationCreditFails() {
        UUID sourceId = UUID.randomUUID();
        UUID destinationId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("100");
        mockActiveAccount(sourceId, "MAD", 500);
        when(accountGateway.getAccount(destinationId))
                .thenReturn(account(destinationId, AccountStatus.ACTIVE, "MAD", 200));
        doThrow(new AccountServiceUnavailableException(new RuntimeException("offline")))
                .when(accountGateway).credit(destinationId, amount);

        assertThatThrownBy(() -> transactionService.transfer(
                sourceId, destinationId, amount, "MAD", "TRANSFER", "MA"))
                .isInstanceOf(TransactionProcessingException.class);
        verify(accountGateway).credit(sourceId, amount);
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void returnsTransactionsByAccount() {
        UUID accountId = UUID.randomUUID();
        Transaction transaction = Transaction.builder().id(UUID.randomUUID()).accountId(accountId).build();
        when(transactionRepository.findByAccountId(accountId)).thenReturn(List.of(transaction));

        assertThat(transactionService.getTransactionsByAccount(accountId)).containsExactly(transaction);
    }

    @Test
    void throwsWhenTransactionDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(transactionRepository.findById(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> transactionService.getTransaction(id))
                .isInstanceOf(TransactionNotFoundException.class);
    }

    private void create(UUID accountId, BigDecimal amount, String currency) {
        transactionService.createTransaction(accountId, amount, currency, "RETAIL", "MA");
    }

    private void mockActiveAccount(UUID accountId, String currency, double balance) {
        when(accountGateway.getAccount(accountId))
                .thenReturn(account(accountId, AccountStatus.ACTIVE, currency, balance));
        when(accountGateway.getBalance(accountId)).thenReturn(new BalanceResponse(accountId, balance));
    }

    private AccountResponse account(UUID id, AccountStatus status, String currency, double balance) {
        return new AccountResponse(id, "CUSTOMER", status, currency, balance);
    }
}
