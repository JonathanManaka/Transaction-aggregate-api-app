package org.example.transacaggrapiapp.categoriser;

import org.example.transacaggrapiapp.model.Category;
import org.example.transacaggrapiapp.model.Transaction;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionCategoriserTest {

    private final TransactionCategoriser categoriser = new TransactionCategoriser();

    @Test
    void shouldCategoriseFoodTransaction() {
        Transaction tx = Transaction.builder().description("Woolworths Food Purchase").build();
        assertThat(categoriser.categorise(tx)).isEqualTo(Category.FOOD);
    }

    @Test
    void shouldCategoriseTransportTransaction() {
        Transaction tx = Transaction.builder().description("Uber Trip to Office").build();
        assertThat(categoriser.categorise(tx)).isEqualTo(Category.TRANSPORT);
    }

    @Test
    void shouldCategoriseEntertainmentTransaction() {
        Transaction tx = Transaction.builder().description("Netflix Monthly Subscription").build();
        assertThat(categoriser.categorise(tx)).isEqualTo(Category.ENTERTAINMENT);
    }

    @Test
    void shouldCategoriseSalaryTransaction() {
        Transaction tx = Transaction.builder().description("Salary from Employer").build();
        assertThat(categoriser.categorise(tx)).isEqualTo(Category.SALARY);
    }

    @Test
    void shouldReturnOtherWhenNoRuleMatches() {
        Transaction tx = Transaction.builder().description("Random unknown payment xyz").build();
        assertThat(categoriser.categorise(tx)).isEqualTo(Category.OTHER);
    }

    @Test
    void shouldNotOverwriteExistingCategory() {
        Transaction tx = Transaction.builder()
                .description("Woolworths Food Purchase")
                .category(Category.SHOPPING)
                .build();

        categoriser.categoriseAndSet(tx);

        // already had a category — should not be overwritten
        assertThat(tx.getCategory()).isEqualTo(Category.SHOPPING);
    }
}
