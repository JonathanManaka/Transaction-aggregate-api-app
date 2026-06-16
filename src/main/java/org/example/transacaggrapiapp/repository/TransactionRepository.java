package org.example.transacaggrapiapp.repository;

import org.example.transacaggrapiapp.model.Category;
import org.example.transacaggrapiapp.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    Optional<Transaction> findByExternalIdAndSource(String externalId, String source);

    List<Transaction> findByCategory(Category category);

    List<Transaction> findByAccountId(String accountId);

    @Query("SELECT t.category, COUNT(t), SUM(t.amount), AVG(t.amount) FROM Transaction t GROUP BY t.category")
    List<Object[]> getCategorySummaries();

    @Query("SELECT FUNCTION('DATE_TRUNC', 'month', t.transactionDate), SUM(t.amount), COUNT(t) " +
            "FROM Transaction t GROUP BY FUNCTION('DATE_TRUNC', 'month', t.transactionDate) " +
            "ORDER BY FUNCTION('DATE_TRUNC', 'month', t.transactionDate)")
    List<Object[]> getMonthlySpendingTrends();
}

