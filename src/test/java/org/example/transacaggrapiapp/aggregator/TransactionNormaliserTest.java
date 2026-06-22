package org.example.transacaggrapiapp.aggregator;

import org.example.transacaggrapiapp.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionNormaliserTest {

    private TransactionNormaliser normaliser;

    @BeforeEach
    void setUp() {
        // fresh instance before every test — good habit even if no state
        normaliser = new TransactionNormaliser();
    }

    @Test
    void shouldDefaultCurrencyToZARWhenNull() {
        Transaction tx = Transaction.builder()
                .description("Woolworths")
                .currency(null)
                .build();

        normaliser.normalise(tx);

        assertThat(tx.getCurrency()).isEqualTo("ZAR");
    }

    @Test
    void shouldDefaultCurrencyToZARWhenBlank() {
        Transaction tx = Transaction.builder()
                .description("Woolworths")
                .currency("   ")
                .build();

        normaliser.normalise(tx);

        assertThat(tx.getCurrency()).isEqualTo("ZAR");
    }

    @Test
    void shouldNotOverwriteCurrencyWhenAlreadySet() {
        Transaction tx = Transaction.builder()
                .description("Foreign Payment")
                .currency("USD")
                .build();

        normaliser.normalise(tx);

        assertThat(tx.getCurrency()).isEqualTo("USD");
    }

    @Test
    void shouldTrimWhitespaceFromDescription() {
        Transaction tx = Transaction.builder()
                .description("  Woolworths Food Purchase  ")
                .currency("ZAR")
                .build();

        normaliser.normalise(tx);

        assertThat(tx.getDescription()).isEqualTo("Woolworths Food Purchase");
    }

    @Test
    void shouldNotFailWhenDescriptionIsNull() {
        Transaction tx = Transaction.builder()
                .description(null)
                .currency("ZAR")
                .build();

        // should not throw NullPointerException
        normaliser.normalise(tx);

        assertThat(tx.getDescription()).isNull();
    }

    @Test
    void shouldReturnTheSameTransactionObject() {
        Transaction tx = Transaction.builder()
                .description("Shell Petrol")
                .currency("ZAR")
                .build();

        Transaction result = normaliser.normalise(tx);

        // normalise returns the same object, not a copy
        assertThat(result).isSameAs(tx);
    }
}
