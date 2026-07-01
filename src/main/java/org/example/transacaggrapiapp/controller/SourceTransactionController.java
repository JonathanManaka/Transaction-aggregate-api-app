package org.example.transacaggrapiapp.controller;

import lombok.RequiredArgsConstructor;
import org.example.transacaggrapiapp.model.SourceTransaction;
import org.example.transacaggrapiapp.service.SourceTransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints for inserting raw source (bank) transactions that the
 * aggregation workflow reads from.
 */
@RestController
@RequestMapping("/api/source-transactions")
@RequiredArgsConstructor
public class SourceTransactionController {

    private final SourceTransactionService sourceTransactionService;

    /**
     * POST /api/source-transactions - insert one or more raw transactions.
     * Body is a JSON array of transactions.
     */
    @PostMapping
    public ResponseEntity<List<SourceTransaction>> insert(@RequestBody List<SourceTransaction> transactions) {
        List<SourceTransaction> saved = sourceTransactionService.insert(transactions);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * GET /api/source-transactions - list all raw source transactions.
     */
    @GetMapping
    public ResponseEntity<List<SourceTransaction>> list() {
        return ResponseEntity.ok(sourceTransactionService.findAll());
    }
}
