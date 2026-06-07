package org.example.transacaggrapiapp.controller;

import lombok.RequiredArgsConstructor;
import org.example.transacaggrapiapp.model.Category;
import org.example.transacaggrapiapp.model.Transaction;
import org.example.transacaggrapiapp.service.TransactionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * GET /api/transactions - List all, filter by date/category/amount
     */
    @GetMapping("/transactions")
    public ResponseEntity<List<Transaction>> getTransactions(
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount) {

        List<Transaction> transactions = transactionService.getFilteredTransactions(
                category, startDate, endDate, minAmount, maxAmount);
        return ResponseEntity.ok(transactions);
    }

    /**
     * GET /api/transactions/{id} - Single transaction
     */
    @GetMapping("/transactions/{id}")
    public ResponseEntity<Transaction> getTransaction(@PathVariable Long id) {
        return transactionService.getTransactionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/aggregate - Manually trigger a sync from all sources
     */
    @PostMapping("/aggregate")
    public ResponseEntity<Map<String, Object>> triggerAggregation() {
        int count = transactionService.triggerAggregation();
        return ResponseEntity.ok(Map.of(
                "status", "completed",
                "newTransactions", count
        ));
    }
}

