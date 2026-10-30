package org.example.transactionservice.service;

import org.example.transactionservice.domain.Transaction;
import org.example.transactionservice.repository.TransactionRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(transactionRepository, new BigDecimal("1000"));
    }

    @Test
    void rejectsNonPositiveAmount() {
        assertThatThrownBy(() -> create(new BigDecimal("0")))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("strictly positive");
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void rejectsAmountAboveConfiguredMaximum() {
        assertThatThrownBy(() -> create(new BigDecimal("1000.01")))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("1000");
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void rejectsMissingRequiredInformation() {
        assertThatThrownBy(() -> transactionService.createTransaction(
                UUID.randomUUID(), BigDecimal.TEN, " ", "RETAIL", "MA"))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("Currency");
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void validTransactionWaitsForAccountIntegration() {
        assertThatThrownBy(() -> create(new BigDecimal("100")))
                .isInstanceOf(AccountIntegrationRequiredException.class)
                .hasMessageContaining("Step C3");
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void validTransferWaitsForAccountIntegration() {
        assertThatThrownBy(() -> transactionService.transfer(
                UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("100"), "MAD"))
                .isInstanceOf(AccountIntegrationRequiredException.class);
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void returnsAllTransactions() {
        Transaction transaction = Transaction.builder().id(UUID.randomUUID()).build();
        when(transactionRepository.findAll()).thenReturn(List.of(transaction));

        assertThat(transactionService.getAllTransactions()).containsExactly(transaction);
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
                .isInstanceOf(TransactionNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    private void create(BigDecimal amount) {
        transactionService.createTransaction(
                UUID.randomUUID(), amount, "MAD", "RETAIL", "MA");
    }
}
