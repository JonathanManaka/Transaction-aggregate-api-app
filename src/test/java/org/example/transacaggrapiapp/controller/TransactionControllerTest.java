package org.example.transacaggrapiapp.controller;

import org.example.transacaggrapiapp.model.Category;
import org.example.transacaggrapiapp.model.Transaction;
import org.example.transacaggrapiapp.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    TransactionService transactionService;

    @InjectMocks
    TransactionController transactionController;

    @Test
    void shouldReturn200WithListOfTransactions() {
        Transaction tx = Transaction.builder()
                .id(1L)
                .description("Woolworths Food Purchase")
                .amount(new BigDecimal("450.99"))
                .category(Category.FOOD)
                .build();

        when(transactionService.getFilteredTransactions(any(), any(), any(), any(), any()))
                .thenReturn(List.of(tx));

        ResponseEntity<List<Transaction>> response = transactionController.getTransactions(
                null, null, null, null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getDescription()).isEqualTo("Woolworths Food Purchase");
        assertThat(response.getBody().get(0).getCategory()).isEqualTo(Category.FOOD);
    }

    @Test
    void shouldReturn200WithSingleTransaction() {
        Transaction tx = Transaction.builder()
                .id(1L)
                .description("Shell Petrol")
                .category(Category.TRANSPORT)
                .build();

        when(transactionService.getTransactionById(1L)).thenReturn(Optional.of(tx));

        ResponseEntity<Transaction> response = transactionController.getTransaction(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getDescription()).isEqualTo("Shell Petrol");
    }

    @Test
    void shouldReturn404WhenTransactionNotFound() {
        when(transactionService.getTransactionById(99L)).thenReturn(Optional.empty());

        ResponseEntity<Transaction> response = transactionController.getTransaction(99L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturn200AndCountWhenAggregationTriggered() {
        when(transactionService.triggerAggregation()).thenReturn(8);

        ResponseEntity<Map<String, Object>> response = transactionController.triggerAggregation();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("status")).isEqualTo("completed");
        assertThat(response.getBody().get("newTransactions")).isEqualTo(8);
    }
}
