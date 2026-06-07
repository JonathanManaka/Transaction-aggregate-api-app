package org.example.transacaggrapiapp.repository;

import org.example.transacaggrapiapp.model.Category;
import org.example.transacaggrapiapp.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByExternalIdAndSource(String externalId, String source);

    List<Transaction> findByCategory(Category category);

    List<Transaction> findByAccountId(String accountId);

    List<Transaction> findByTransactionDateBetween(LocalDateTime start, LocalDateTime end);

    List<Transaction> findByCategoryAndTransactionDateBetween(Category category, LocalDateTime start, LocalDateTime end);

    @Query("SELECT t FROM Transaction t WHERE " +
            "(:category IS NULL OR t.category = :category) AND " +
            "(:startDate IS NULL OR t.transactionDate >= :startDate) AND " +
            "(:endDate IS NULL OR t.transactionDate <= :endDate) AND " +
            "(:minAmount IS NULL OR t.amount >= :minAmount) AND " +
            "(:maxAmount IS NULL OR t.amount <= :maxAmount)")
    List<Transaction> findWithFilters(
            @Param("category") Category category,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount);

    @Query("SELECT t.category, COUNT(t), SUM(t.amount), AVG(t.amount) FROM Transaction t GROUP BY t.category")
    List<Object[]> getCategorySummaries();

    @Query("SELECT FUNCTION('DATE_TRUNC', 'month', t.transactionDate), SUM(t.amount), COUNT(t) " +
            "FROM Transaction t GROUP BY FUNCTION('DATE_TRUNC', 'month', t.transactionDate) " +
            "ORDER BY FUNCTION('DATE_TRUNC', 'month', t.transactionDate)")
    List<Object[]> getMonthlySpendingTrends();
}

