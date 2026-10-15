package com.mvimbi.aggregator.service;

import com.mvimbi.aggregator.domain.Category;
import com.mvimbi.aggregator.domain.SourceSystem;
import com.mvimbi.aggregator.domain.TransactionType;
import com.mvimbi.aggregator.web.InvalidRequestException;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents the set of optional filtering criteria available for
 * transaction search operations.
 *
 * Filters may be applied based on transaction date range, category,
 * source system, transaction type, amount range, and free-text search
 * parameters. When amount-based filtering is applied, the minimum and
 * maximum values are evaluated against the absolute value of the
 * transaction amount, regardless of whether the transaction represents
 * a debit or credit.
 *
 * The validate() method ensures that all supplied filter values are
 * logically consistent before processing the request.
 *
 * @author Athenkosi Mvimbi
 */

public record TransactionFilter(
        LocalDate from,
        LocalDate to,
        Category category,
        SourceSystem source,
        TransactionType type,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        String q) {

    public void validate() {
        if (from != null && to != null && from.isAfter(to)) {
            throw new InvalidRequestException("'from' must not be after 'to'");
        }
        if (minAmount != null && minAmount.signum() < 0) {
            throw new InvalidRequestException("'minAmount' must not be negative");
        }
        if (maxAmount != null && maxAmount.signum() < 0) {
            throw new InvalidRequestException("'maxAmount' must not be negative");
        }
        if (minAmount != null && maxAmount != null && minAmount.compareTo(maxAmount) > 0) {
            throw new InvalidRequestException("'minAmount' must not be greater than 'maxAmount'");
        }
    }
}
