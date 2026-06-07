package org.example.transacaggrapiapp.aggregator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.transacaggrapiapp.categoriser.TransactionCategoriser;
import org.example.transacaggrapiapp.model.Transaction;
import org.example.transacaggrapiapp.repository.TransactionRepository;
import org.example.transacaggrapiapp.source.MockBankAClient;
import org.example.transacaggrapiapp.source.MockBankBClient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates fetching from all sources, normalising, categorising, and persisting.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionAggregator {

    private final MockBankAClient bankAClient;
    private final MockBankBClient bankBClient;
    private final TransactionNormaliser normaliser;
    private final TransactionCategoriser categoriser;
    private final TransactionRepository transactionRepository;

    /**
     * Aggregate transactions from all sources.
     * @return number of new transactions saved
     */
    public int aggregate() {
        log.info("Starting transaction aggregation from all sources...");

        List<Transaction> allTransactions = new ArrayList<>();
        allTransactions.addAll(bankAClient.fetchTransactions());
        allTransactions.addAll(bankBClient.fetchTransactions());

        int saved = 0;
        for (Transaction tx : allTransactions) {
            // Skip duplicates
            if (transactionRepository.findByExternalIdAndSource(tx.getExternalId(), tx.getSource()).isPresent()) {
                log.debug("Skipping duplicate: {} from {}", tx.getExternalId(), tx.getSource());
                continue;
            }

            // Normalise and categorise
            normaliser.normalise(tx);
            categoriser.categoriseAndSet(tx);

            transactionRepository.save(tx);
            saved++;
        }

        log.info("Aggregation complete. {} new transactions saved.", saved);
        return saved;
    }
}

