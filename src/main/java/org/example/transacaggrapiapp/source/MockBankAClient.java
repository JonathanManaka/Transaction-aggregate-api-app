package org.example.transacaggrapiapp.source;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.transacaggrapiapp.model.SourceTransaction;
import org.example.transacaggrapiapp.model.Transaction;
import org.example.transacaggrapiapp.repository.SourceTransactionRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Fetches Bank A transactions from the source_transactions table.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MockBankAClient {

    private static final String SOURCE = "BANK_A";

    private final SourceTransactionRepository sourceTransactionRepository;

    public List<Transaction> fetchTransactions() {
        log.info("Fetching {} transactions from source_transactions table", SOURCE);

        return sourceTransactionRepository.findBySource(SOURCE).stream()
                .map(MockBankAClient::toTransaction)
                .toList();
    }

    private static Transaction toTransaction(SourceTransaction s) {
        return Transaction.builder()
                .externalId(s.getExternalId())
                .source(s.getSource())
                .description(s.getDescription())
                .amount(s.getAmount())
                .currency(s.getCurrency())
                .transactionDate(s.getTransactionDate())
                .accountId(s.getAccountId())
                .build();
    }
}
