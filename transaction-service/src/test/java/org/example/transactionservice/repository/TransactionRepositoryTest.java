package org.example.transactionservice.repository;

import org.example.transactionservice.domain.Transaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    void persistsAndFindsTransactionByAccount() {
        UUID accountId = UUID.randomUUID();
        Transaction transaction = Transaction.builder()
                .accountId(accountId)
                .amount(new BigDecimal("125.50"))
                .currency("MAD")
                .merchantCategory("RETAIL")
                .country("MA")
                .createdAt(Instant.now())
                .build();

        Transaction saved = transactionRepository.saveAndFlush(transaction);

        assertThat(saved.getId()).isNotNull();
        assertThat(transactionRepository.findByAccountId(accountId))
                .singleElement()
                .extracting(Transaction::getAmount)
                .isEqualTo(new BigDecimal("125.50"));
    }
}
