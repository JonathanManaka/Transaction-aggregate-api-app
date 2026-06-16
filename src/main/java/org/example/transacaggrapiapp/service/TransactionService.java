package org.example.transacaggrapiapp.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.example.transacaggrapiapp.aggregator.TransactionAggregator;
import org.example.transacaggrapiapp.model.Category;
import org.example.transacaggrapiapp.model.Transaction;
import org.example.transacaggrapiapp.model.TransactionSummary;
import org.example.transacaggrapiapp.repository.TransactionRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
        Specification<Transaction> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (category != null)   predicates.add(cb.equal(root.get("category"), category));
            if (startDate != null)  predicates.add(cb.greaterThanOrEqualTo(root.get("transactionDate"), startDate));
            if (endDate != null)    predicates.add(cb.lessThanOrEqualTo(root.get("transactionDate"), endDate));
            if (minAmount != null)  predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), minAmount));
            if (maxAmount != null)  predicates.add(cb.lessThanOrEqualTo(root.get("amount"), maxAmount));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return transactionRepository.findAll(spec);
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

