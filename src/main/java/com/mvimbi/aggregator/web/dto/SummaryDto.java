package com.mvimbi.aggregator.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** @author Athenkosi Mvimbi */
public record SummaryDto(
        Long customerId,
        String currency,
        LocalDate from,
        LocalDate to,
        long transactionCount,
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal netCashflow,
        BigDecimal savingsRatePercent) {
}
