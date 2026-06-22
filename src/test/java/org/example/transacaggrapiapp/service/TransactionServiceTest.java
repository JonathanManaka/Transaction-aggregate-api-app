package org.example.transacaggrapiapp.service;

import org.example.transacaggrapiapp.aggregator.TransactionAggregator;
import org.example.transacaggrapiapp.model.Category;
import org.example.transacaggrapiapp.model.Transaction;
import org.example.transacaggrapiapp.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    TransactionRepository transactionRepository;

    @Mock
    TransactionAggregator transactionAggregator;

    @InjectMocks
    TransactionService transactionService;

    @Test
    void shouldReturnTransactionById() {
        Transaction tx = Transaction.builder()
                .id(1L)
                .description("Woolworths Food Purchase")
                .category(Category.FOOD)
                .build();

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(tx));

        Optional<Transaction> result = transactionService.getTransactionById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getDescription()).isEqualTo("Woolworths Food Purchase");
    }

    @Test
    void shouldReturnEmptyWhenTransactionNotFound() {
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Transaction> result = transactionService.getTransactionById(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnAllTransactionsWhenNoFiltersApplied() {
        Transaction tx1 = Transaction.builder().id(1L).description("Woolworths").category(Category.FOOD).build();
        Transaction tx2 = Transaction.builder().id(2L).description("Shell Petrol").category(Category.TRANSPORT).build();

        when(transactionRepository.findAll(any(Specification.class))).thenReturn(List.of(tx1, tx2));

        List<Transaction> result = transactionService.getFilteredTransactions(null, null, null, null, null);

        assertThat(result).hasSize(2);
    }

    @Test
    void shouldFilterByCategory() {
        Transaction tx = Transaction.builder().id(1L).description("Woolworths").category(Category.FOOD).build();

        when(transactionRepository.findAll(any(Specification.class))).thenReturn(List.of(tx));

        List<Transaction> result = transactionService.getFilteredTransactions(Category.FOOD, null, null, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory()).isEqualTo(Category.FOOD);
    }

    @Test
    void shouldTriggerAggregationAndReturnCount() {
        when(transactionAggregator.aggregate()).thenReturn(8);

        int count = transactionService.triggerAggregation();

        assertThat(count).isEqualTo(8);
        verify(transactionAggregator).aggregate();
    }
}
