package com.mvimbi.aggregator.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Centralised utility for monetary value handling.
 *
 * This application operates exclusively in South African Rand (ZAR), so
 * currency is treated as a system-wide constant rather than being persisted
 * on individual entities.
 *
 * All amounts are normalised to two decimal places using banker's rounding
 * (HALF_EVEN) to ensure consistent financial calculations throughout the
 * application.
 *
 * @author Athenkosi Mvimbi
 */

public final class Money {

    public static final String CURRENCY = "ZAR";
    private static final int SCALE = 2; // cents

    private Money() {
    }

    /** Normalises an amount to 2 decimal places (rands and cents). */
    public static BigDecimal zar(BigDecimal value) {
        return value == null ? null : value.setScale(SCALE, RoundingMode.HALF_EVEN);
    }

    public static BigDecimal zero() {
        return BigDecimal.ZERO.setScale(SCALE);
    }
}
