package org.example.accountservie.repository;

import org.example.accountservie.domain.Account;
import org.example.accountservie.domain.AccountStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void persistsAndRetrievesAccount() {
        Account account = Account.builder()
                .customerReference("CUSTOMER-2")
                .status(AccountStatus.ACTIVE)
                .currency("EUR")
                .balance(250)
                .build();

        Account saved = accountRepository.saveAndFlush(account);
        Account retrieved = accountRepository.findById(saved.getId()).orElseThrow();

        assertThat(saved.getId()).isNotNull();
        assertThat(retrieved.getCustomerReference()).isEqualTo("CUSTOMER-2");
        assertThat(retrieved.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(retrieved.getCurrency()).isEqualTo("EUR");
        assertThat(retrieved.getBalance()).isEqualTo(250);
    }
}
