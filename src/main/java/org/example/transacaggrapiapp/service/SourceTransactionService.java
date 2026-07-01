package org.example.transacaggrapiapp.service;

import lombok.RequiredArgsConstructor;
import org.example.transacaggrapiapp.model.SourceTransaction;
import org.example.transacaggrapiapp.repository.SourceTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SourceTransactionService {

    private final SourceTransactionRepository sourceTransactionRepository;

    @Transactional
    public List<SourceTransaction> insert(List<SourceTransaction> transactions) {
        // Ignore any client-supplied id / createdAt so they are DB-generated.
        List<SourceTransaction> toSave = transactions.stream()
                .map(t -> SourceTransaction.builder()
                        .externalId(t.getExternalId())
                        .source(t.getSource())
                        .description(t.getDescription())
                        .amount(t.getAmount())
                        .currency(t.getCurrency())
                        .transactionDate(t.getTransactionDate())
                        .accountId(t.getAccountId())
                        .build())
                .toList();
        return sourceTransactionRepository.saveAll(toSave);
    }

    @Transactional(readOnly = true)
    public List<SourceTransaction> findAll() {
        return sourceTransactionRepository.findAll();
    }
}
