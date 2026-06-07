package org.example.transacaggrapiapp.source;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.transacaggrapiapp.model.Transaction;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Mock client simulating fetching transactions from Bank B's REST API.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MockBankBClient {

    private final RestClient restClient;

    /**
     * In production this would call Bank B's API.
     * For now, returns mock transaction data.
     */
    public List<Transaction> fetchTransactions() {
        log.info("Fetching transactions from Bank B (mock)");

        return List.of(
                Transaction.builder()
                        .externalId("BANKB-001")
                        .source("BANK_B")
                        .description("Checkers Groceries")
                        .amount(new BigDecimal("320.75"))
                        .currency("ZAR")
                        .transactionDate(LocalDateTime.now().minusDays(1))
                        .accountId("ACC-2001")
                        .build(),
                Transaction.builder()
                        .externalId("BANKB-002")
                        .source("BANK_B")
                        .description("Eskom Electricity Payment")
                        .amount(new BigDecimal("1200.00"))
                        .currency("ZAR")
                        .transactionDate(LocalDateTime.now().minusDays(3))
                        .accountId("ACC-2001")
                        .build(),
                Transaction.builder()
                        .externalId("BANKB-003")
                        .source("BANK_B")
                        .description("Takealot Online Shopping")
                        .amount(new BigDecimal("2499.99"))
                        .currency("ZAR")
                        .transactionDate(LocalDateTime.now().minusDays(4))
                        .accountId("ACC-2001")
                        .build(),
                Transaction.builder()
                        .externalId("BANKB-004")
                        .source("BANK_B")
                        .description("Discovery Health Premium")
                        .amount(new BigDecimal("3500.00"))
                        .currency("ZAR")
                        .transactionDate(LocalDateTime.now().minusDays(7))
                        .accountId("ACC-2001")
                        .build()
        );
    }
}

