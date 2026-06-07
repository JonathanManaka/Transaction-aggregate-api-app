package org.example.transacaggrapiapp.controller;

import lombok.RequiredArgsConstructor;
import org.example.transacaggrapiapp.model.TransactionSummary;
import org.example.transacaggrapiapp.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SummaryController {

    private final TransactionService transactionService;

    /**
     * GET /api/summary - Totals grouped by category
     */
    @GetMapping("/summary")
    public ResponseEntity<List<TransactionSummary>> getSummary() {
        return ResponseEntity.ok(transactionService.getCategorySummaries());
    }

    /**
     * GET /api/categories - Breakdown per category with counts
     */
    @GetMapping("/categories")
    public ResponseEntity<List<TransactionSummary>> getCategories() {
        return ResponseEntity.ok(transactionService.getCategorySummaries());
    }
}

