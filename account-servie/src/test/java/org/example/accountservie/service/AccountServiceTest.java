package org.example.accountservie.service;

import org.example.accountservie.domain.Account;
import org.example.accountservie.domain.AccountStatus;
import org.example.accountservie.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(accountRepository, 1000);
        lenient().when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createsAccount() {
        Account created = accountService.createAccount("CUSTOMER-1", AccountStatus.ACTIVE, "MAD", 500);

        assertThat(created.getCustomerReference()).isEqualTo("CUSTOMER-1");
        assertThat(created.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(created.getCurrency()).isEqualTo("MAD");
        assertThat(created.getBalance()).isEqualTo(500);
        verify(accountRepository).save(created);
    }

    @Test
    void throwsWhenAccountDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(accountRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccount(id))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void debitsActiveAccount() {
        Account account = account(AccountStatus.ACTIVE, 500);
        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));

        Account result = accountService.debit(account.getId(), 125);

        assertThat(result.getBalance()).isEqualTo(375);
        verify(accountRepository).save(account);
    }

    @Test
    void creditsActiveAccount() {
        Account account = account(AccountStatus.ACTIVE, 500);
        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));

        Account result = accountService.credit(account.getId(), 75);

        assertThat(result.getBalance()).isEqualTo(575);
        verify(accountRepository).save(account);
    }

    @Test
    void rejectsOperationOnBlockedAccount() {
        Account account = account(AccountStatus.BLOCKED, 500);
        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.debit(account.getId(), 50))
                .isInstanceOf(AccountBlockedException.class);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void rejectsInvalidAmount() {
        assertThatThrownBy(() -> accountService.credit(UUID.randomUUID(), 0))
                .isInstanceOf(InvalidAmountException.class);
        assertThatThrownBy(() -> accountService.debit(UUID.randomUUID(), -1))
                .isInstanceOf(InvalidAmountException.class);
        assertThatThrownBy(() -> accountService.credit(UUID.randomUUID(), Double.NaN))
                .isInstanceOf(InvalidAmountException.class);
        verifyNoInteractions(accountRepository);
    }

    @Test
    void rejectsAmountAboveConfiguredMaximum() {
        assertThatThrownBy(() -> accountService.debit(UUID.randomUUID(), 1000.01))
                .isInstanceOf(InvalidAmountException.class)
                .hasMessageContaining("1000");
        verifyNoInteractions(accountRepository);
    }

    @Test
    void rejectsDebitWithInsufficientBalance() {
        Account account = account(AccountStatus.ACTIVE, 40);
        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.debit(account.getId(), 50))
                .isInstanceOf(InsufficientBalanceException.class);
        assertThat(account.getBalance()).isEqualTo(40);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void updatesAccountStatus() {
        Account account = account(AccountStatus.ACTIVE, 500);
        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));

        Account result = accountService.updateStatus(account.getId(), AccountStatus.BLOCKED);

        assertThat(result.getStatus()).isEqualTo(AccountStatus.BLOCKED);
    }

    private Account account(AccountStatus status, double balance) {
        return Account.builder()
                .id(UUID.randomUUID())
                .customerReference("CUSTOMER-1")
                .status(status)
                .currency("MAD")
                .balance(balance)
                .build();
    }
}
