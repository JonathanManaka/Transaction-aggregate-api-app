package org.example.transacaggrapiapp.aggregator;

import org.example.transacaggrapiapp.model.Transaction;
import org.springframework.stereotype.Component;

/**
 * Normalises transaction data from different sources into a unified model.
 */
@Component
public class TransactionNormaliser {

    /**
     * Ensures all required fields are set and formats are consistent.
     */
    public Transaction normalise(Transaction transaction) {
        // Default currency to ZAR if not set
        if (transaction.getCurrency() == null || transaction.getCurrency().isBlank()) {
            transaction.setCurrency("ZAR");
        }

        // Trim description
        if (transaction.getDescription() != null) {
            transaction.setDescription(transaction.getDescription().trim());
        }

        return transaction;
    }
}

