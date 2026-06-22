package org.example.transacaggrapiapp.aggregator;

import org.example.transacaggrapiapp.categoriser.TransactionCategoriser;
import org.example.transacaggrapiapp.model.Category;
import org.example.transacaggrapiapp.model.Transaction;
import org.example.transacaggrapiapp.repository.TransactionRepository;
import org.example.transacaggrapiapp.source.MockBankAClient;
import org.example.transacaggrapiapp.source.MockBankBClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionAggregatorTest {

    @Mock
    MockBankAClient bankAClient;

    @Mock
    MockBankBClient bankBClient;

    @Mock
    TransactionNormaliser normaliser;

    @Mock
    TransactionCategoriser categoriser;

    @Mock
    TransactionRepository transactionRepository;

    @InjectMocks
    TransactionAggregator aggregator;

    // helper to build a transaction quickly
    private Transaction buildTx(String externalId, String source) {
        return Transaction.builder()
                .externalId(externalId)
                .source(source)
                .description("Test transaction")
                .amount(new BigDecimal("100.00"))
                .currency("ZAR")
                .category(Category.OTHER)
                .transactionDate(LocalDateTime.now())
                .accountId("ACC-001")
                .build();
    }

    @Test
    void shouldSaveNewTransactionsFromBothBanks() {
        Transaction txA = buildTx("BANKA-001", "BANK_A");
        Transaction txB = buildTx("BANKB-001", "BANK_B");

        when(bankAClient.fetchTransactions()).thenReturn(List.of(txA));
        when(bankBClient.fetchTransactions()).thenReturn(List.of(txB));

        // both are new — not in DB yet
        when(transactionRepository.findByExternalIdAndSource("BANKA-001", "BANK_A"))
                .thenReturn(Optional.empty());
        when(transactionRepository.findByExternalIdAndSource("BANKB-001", "BANK_B"))
                .thenReturn(Optional.empty());

        // normaliser and categoriser return the same transaction passed in
        when(normaliser.normalise(any())).thenAnswer(inv -> inv.getArgument(0));
        when(categoriser.categoriseAndSet(any())).thenAnswer(inv -> inv.getArgument(0));

        int saved = aggregator.aggregate();

        assertThat(saved).isEqualTo(2);
        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }

    @Test
    void shouldSkipDuplicateTransactions() {
        Transaction txA = buildTx("BANKA-001", "BANK_A");

        when(bankAClient.fetchTransactions()).thenReturn(List.of(txA));
        when(bankBClient.fetchTransactions()).thenReturn(List.of());

        // already exists in DB
        when(transactionRepository.findByExternalIdAndSource("BANKA-001", "BANK_A"))
                .thenReturn(Optional.of(txA));

        int saved = aggregator.aggregate();

        assertThat(saved).isEqualTo(0);
        // save should never be called for a duplicate
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void shouldNormaliseAndCategoriseBeforeSaving() {
        Transaction tx = buildTx("BANKA-001", "BANK_A");

        when(bankAClient.fetchTransactions()).thenReturn(List.of(tx));
        when(bankBClient.fetchTransactions()).thenReturn(List.of());
        when(transactionRepository.findByExternalIdAndSource(any(), any()))
                .thenReturn(Optional.empty());
        when(normaliser.normalise(any())).thenAnswer(inv -> inv.getArgument(0));
        when(categoriser.categoriseAndSet(any())).thenAnswer(inv -> inv.getArgument(0));

        aggregator.aggregate();

        // verify normaliser and categoriser were both called before saving
        verify(normaliser).normalise(tx);
        verify(categoriser).categoriseAndSet(tx);
        verify(transactionRepository).save(tx);
    }

    @Test
    void shouldReturnZeroWhenBothBanksReturnEmpty() {
        when(bankAClient.fetchTransactions()).thenReturn(List.of());
        when(bankBClient.fetchTransactions()).thenReturn(List.of());

        int saved = aggregator.aggregate();

        assertThat(saved).isEqualTo(0);
        verify(transactionRepository, never()).save(any());
    }
}
