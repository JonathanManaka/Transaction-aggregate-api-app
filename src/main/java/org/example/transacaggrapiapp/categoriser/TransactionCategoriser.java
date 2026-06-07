package org.example.transacaggrapiapp.categoriser;

import org.example.transacaggrapiapp.model.Category;
import org.example.transacaggrapiapp.model.Transaction;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Classifies transactions into categories based on description keywords.
 */
@Component
public class TransactionCategoriser {

    private static final Map<Pattern, Category> RULES = Map.ofEntries(
            Map.entry(Pattern.compile("(?i).*(grocery|groceries|food|woolworths|pick n pay|checkers|spar|restaurant|kfc|nandos|mcdonalds).*"), Category.FOOD),
            Map.entry(Pattern.compile("(?i).*(uber|bolt|petrol|shell|engen|sasol|parking|toll|transport|taxi).*"), Category.TRANSPORT),
            Map.entry(Pattern.compile("(?i).*(takealot|amazon|zara|h&m|clothing|shop|mall|store).*"), Category.SHOPPING),
            Map.entry(Pattern.compile("(?i).*(eskom|electricity|water|internet|fibre|vodacom|mtn|telkom|dstv|rates).*"), Category.UTILITIES),
            Map.entry(Pattern.compile("(?i).*(netflix|spotify|cinema|movie|game|concert|entertainment).*"), Category.ENTERTAINMENT),
            Map.entry(Pattern.compile("(?i).*(discovery|medical|health|pharmacy|doctor|hospital|clinic).*"), Category.HEALTH),
            Map.entry(Pattern.compile("(?i).*(salary|wage|income|deposit from employer).*"), Category.SALARY),
            Map.entry(Pattern.compile("(?i).*(transfer|payment to|eft|send money).*"), Category.TRANSFER)
    );

    /**
     * Categorise a single transaction based on its description.
     */
    public Category categorise(Transaction transaction) {
        String description = transaction.getDescription();
        for (Map.Entry<Pattern, Category> rule : RULES.entrySet()) {
            if (rule.getKey().matcher(description).matches()) {
                return rule.getValue();
            }
        }
        return Category.OTHER;
    }

    /**
     * Apply categorisation to a transaction (mutates the category field).
     */
    public Transaction categoriseAndSet(Transaction transaction) {
        if (transaction.getCategory() == null) {
            transaction.setCategory(categorise(transaction));
        }
        return transaction;
    }
}

