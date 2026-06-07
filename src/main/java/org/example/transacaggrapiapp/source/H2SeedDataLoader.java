package org.example.transacaggrapiapp.source;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.transacaggrapiapp.model.Category;
import org.example.transacaggrapiapp.model.Transaction;
import org.example.transacaggrapiapp.repository.TransactionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Seeds the H2 database with test transactions when running in 'dev' profile.
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class H2SeedDataLoader implements CommandLineRunner {

    private final TransactionRepository transactionRepository;

    @Override
    public void run(String... args) {
        if (transactionRepository.count() > 0) {
            log.info("Database already seeded, skipping.");
            return;
        }

        log.info("Seeding H2 database with test transactions...");

        List<Transaction> seeds = List.of(
                Transaction.builder()
                        .externalId("SEED-001").source("SEED").description("Pick n Pay Groceries")
                        .amount(new BigDecimal("675.30")).currency("ZAR").category(Category.FOOD)
                        .transactionDate(LocalDateTime.now().minusDays(2)).accountId("ACC-SEED-01").build(),
                Transaction.builder()
                        .externalId("SEED-002").source("SEED").description("Shell Petrol")
                        .amount(new BigDecimal("950.00")).currency("ZAR").category(Category.TRANSPORT)
                        .transactionDate(LocalDateTime.now().minusDays(3)).accountId("ACC-SEED-01").build(),
                Transaction.builder()
                        .externalId("SEED-003").source("SEED").description("Zara Clothing")
                        .amount(new BigDecimal("1899.99")).currency("ZAR").category(Category.SHOPPING)
                        .transactionDate(LocalDateTime.now().minusDays(5)).accountId("ACC-SEED-01").build(),
                Transaction.builder()
                        .externalId("SEED-004").source("SEED").description("City of Cape Town Water")
                        .amount(new BigDecimal("450.00")).currency("ZAR").category(Category.UTILITIES)
                        .transactionDate(LocalDateTime.now().minusDays(7)).accountId("ACC-SEED-01").build(),
                Transaction.builder()
                        .externalId("SEED-005").source("SEED").description("Salary from Employer")
                        .amount(new BigDecimal("42000.00")).currency("ZAR").category(Category.SALARY)
                        .transactionDate(LocalDateTime.now().minusDays(15)).accountId("ACC-SEED-01").build()
        );

        transactionRepository.saveAll(seeds);
        log.info("Seeded {} transactions.", seeds.size());
    }
}

