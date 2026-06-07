package org.example.transacaggrapiapp.service;

import lombok.RequiredArgsConstructor;
import org.example.transacaggrapiapp.aggregator.TransactionAggregator;
import org.example.transacaggrapiapp.model.Category;
import org.example.transacaggrapiapp.model.Transaction;
import org.example.transacaggrapiapp.model.TransactionSummary;
import org.example.transacaggrapiapp.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionAggregator transactionAggregator;

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public Optional<Transaction> getTransactionById(Long id) {
        return transactionRepository.findById(id);
    }

    public List<Transaction> getFilteredTransactions(Category category, LocalDateTime startDate,
                                                     LocalDateTime endDate, BigDecimal minAmount,
                                                     BigDecimal maxAmount) {
        return transactionRepository.findWithFilters(category, startDate, endDate, minAmount, maxAmount);
    }

    public List<TransactionSummary> getCategorySummaries() {
        return transactionRepository.getCategorySummaries().stream()
                .map(row -> TransactionSummary.builder()
                        .category((Category) row[0])
                        .count((Long) row[1])
                        .totalAmount((BigDecimal) row[2])
                        .averageAmount(BigDecimal.valueOf((Double) row[3]))
                        .build())
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getMonthlySpendingTrends() {
        return transactionRepository.getMonthlySpendingTrends().stream()
                .map(row -> Map.<String, Object>of(
                        "month", row[0].toString(),
                        "totalAmount", row[1],
                        "transactionCount", row[2]
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public int triggerAggregation() {
        return transactionAggregator.aggregate();
    }
}

