package org.example.transacaggrapiapp.repository;

import org.example.transacaggrapiapp.model.Category;
import org.example.transacaggrapiapp.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("testcontainers")
class TransactionRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .waitingFor(Wait.forListeningPort());

    @Autowired
    TransactionRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        repository.save(transaction("ext-1", "BankA", "Woolworths Food", new BigDecimal("500.00"), Category.FOOD,
                LocalDateTime.of(2024, 1, 10, 9, 0)));
        repository.save(transaction("ext-2", "BankA", "Shell Petrol", new BigDecimal("800.00"), Category.TRANSPORT,
                LocalDateTime.of(2024, 1, 20, 14, 0)));
        repository.save(transaction("ext-3", "BankB", "Netflix", new BigDecimal("199.00"), Category.ENTERTAINMENT,
                LocalDateTime.of(2024, 2, 5, 18, 0)));
        repository.save(transaction("ext-4", "BankB", "Pick n Pay", new BigDecimal("350.00"), Category.FOOD,
                LocalDateTime.of(2024, 2, 15, 11, 0)));
    }

    @Test
    void shouldGroupMonthlySpendingByDateTrunc() {
        List<Object[]> trends = repository.getMonthlySpendingTrends();

        assertThat(trends).hasSize(2);

        // January: 500 + 800 = 1300, count 2
        Object[] january = trends.get(0);
        assertThat(((BigDecimal) january[1]).compareTo(new BigDecimal("1300.00"))).isZero();
        assertThat(january[2]).isEqualTo(2L);

        // February: 199 + 350 = 549, count 2
        Object[] february = trends.get(1);
        assertThat(((BigDecimal) february[1]).compareTo(new BigDecimal("549.00"))).isZero();
        assertThat(february[2]).isEqualTo(2L);
    }

    @Test
    void shouldReturnEmptyTrendsWhenNoTransactions() {
        repository.deleteAll();

        List<Object[]> trends = repository.getMonthlySpendingTrends();

        assertThat(trends).isEmpty();
    }

    @Test
    void shouldGroupCategorySummariesCorrectly() {
        List<Object[]> summaries = repository.getCategorySummaries();

        assertThat(summaries).hasSize(3);

        summaries.forEach(row -> {
            Category cat = (Category) row[0];
            Long count = (Long) row[1];
            if (cat == Category.FOOD) {
                assertThat(count).isEqualTo(2L);
            } else {
                assertThat(count).isEqualTo(1L);
            }
        });
    }

    @Test
    void shouldFindByExternalIdAndSource() {
        var result = repository.findByExternalIdAndSource("ext-1", "BankA");

        assertThat(result).isPresent();
        assertThat(result.get().getDescription()).isEqualTo("Woolworths Food");
    }

    private Transaction transaction(String externalId, String source, String description,
                                    BigDecimal amount, Category category, LocalDateTime date) {
        return Transaction.builder()
                .externalId(externalId)
                .source(source)
                .description(description)
                .amount(amount)
                .currency("ZAR")
                .category(category)
                .transactionDate(date)
                .accountId("ACC-001")
                .build();
    }
}
