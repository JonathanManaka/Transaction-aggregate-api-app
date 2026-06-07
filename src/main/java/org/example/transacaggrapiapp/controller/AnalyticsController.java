package org.example.transacaggrapiapp.controller;

import lombok.RequiredArgsConstructor;
import org.example.transacaggrapiapp.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AnalyticsController {

    private final TransactionService transactionService;

    /**
     * GET /api/analytics - Spending trends over time (monthly)
     */
    @GetMapping("/analytics")
    public ResponseEntity<List<Map<String, Object>>> getAnalytics() {
        return ResponseEntity.ok(transactionService.getMonthlySpendingTrends());
    }
}

