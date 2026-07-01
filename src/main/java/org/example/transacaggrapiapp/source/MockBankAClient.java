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
 * Mock client simulating fetching transactions from Bank A's REST API.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MockBankAClient {

    private final RestClient restClient;

    /**
     * In production this would call Bank A's API.
     * For now, returns mock transaction data.
     */
    public List<Transaction> fetchTransactions() {
        log.info("Fetching transactions from Bank A (mock)");

        return List.of(
                Transaction.builder()
                        .externalId("BANKA-001")
                        .source("BANK_A")
                        .description("Woolworths Food Purchase")
                        .amount(new BigDecimal("450.99"))
                        .currency("ZAR")
                        .transactionDate(LocalDateTime.now().minusDays(1))
                        .accountId("ACC-1001")
                        .build(),
                Transaction.builder()
                        .externalId("BANKA-002")
                        .source("BANK_A")
                        .description("Uber Trip to Office")
                        .amount(new BigDecimal("89.50"))
                        .currency("ZAR")
                        .transactionDate(LocalDateTime.now().minusDays(2))
                        .accountId("ACC-1001")
                        .build(),
                Transaction.builder()
                        .externalId("BANKA-003")
                        .source("BANK_A")
                        .description("Netflix Monthly Subscription")
                        .amount(new BigDecimal("199.00"))
                        .currency("ZAR")
                        .transactionDate(LocalDateTime.now().minusDays(5))
                        .accountId("ACC-1001")
                        .build(),
                Transaction.builder()
                        .externalId("BANKA-004")
                        .source("BANK_A")
                        .description("Salary Deposit")
                        .amount(new BigDecimal("35000.00"))
                        .currency("ZAR")
                        .transactionDate(LocalDateTime.now().minusDays(10))
                        .accountId("ACC-1001")
                        .build(),
                Transaction.builder()
                        .externalId("BANKA-005")
                        .source("BANK_A")
                        .description("Salary Deposit")
                        .amount(new BigDecimal("55000.00"))
                        .currency("ZAR")
                        .transactionDate(LocalDateTime.now().minusDays(10))
                        .accountId("ACC-1001")
                        .build()
        );
    }
}

