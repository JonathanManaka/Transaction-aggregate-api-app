package org.example.transacaggrapiapp.repository;

import jakarta.persistence.criteria.Predicate;
import org.example.transacaggrapiapp.model.Category;
import org.example.transacaggrapiapp.model.Transaction;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionSpecification {

    public static Specification<Transaction> withFilters(Category category, LocalDateTime startDate,
                                                         LocalDateTime endDate, BigDecimal minAmount,
                                                         BigDecimal maxAmount) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (category != null)   predicates.add(cb.equal(root.get("category"), category));
            if (startDate != null)  predicates.add(cb.greaterThanOrEqualTo(root.<LocalDateTime>get("transactionDate"), startDate));
            if (endDate != null)    predicates.add(cb.lessThanOrEqualTo(root.<LocalDateTime>get("transactionDate"), endDate));
            if (minAmount != null)  predicates.add(cb.greaterThanOrEqualTo(root.<BigDecimal>get("amount"), minAmount));
            if (maxAmount != null)  predicates.add(cb.lessThanOrEqualTo(root.<BigDecimal>get("amount"), maxAmount));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
