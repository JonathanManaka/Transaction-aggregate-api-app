package org.example.transacaggrapiapp.model;

import lombok.*;

import java.math.BigDecimal;

/**
 * DTO for category summary responses.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionSummary {

    private Category category;
    private long count;
    private BigDecimal totalAmount;
    private BigDecimal averageAmount;
}

