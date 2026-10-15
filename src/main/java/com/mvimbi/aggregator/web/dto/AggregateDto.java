package com.mvimbi.aggregator.web.dto;

import java.math.BigDecimal;

/** One bucket (a category, a month, a merchant...). @author Athenkosi Mvimbi */
public record AggregateDto(
        String key,
        String label,
        long transactionCount,
        BigDecimal totalIn,
        BigDecimal totalOut,
        BigDecimal net,
        BigDecimal shareOfSpendPercent) {
}
