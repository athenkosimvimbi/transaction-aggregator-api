package com.mvimbi.aggregator.repository;

import java.math.BigDecimal;

/**
 * Projection interface used to map aggregated query results.
 *
 * The column aliases returned by the query (label, cnt, totalIn, totalOut)
 * must match the corresponding getter method names for Spring Data JPA
 * interface-based projections to work correctly.
 *
 * @author Athenkosi Mvimbi
 */

public interface AggregateView {

    String getLabel();

    Long getCnt();

    BigDecimal getTotalIn();

    BigDecimal getTotalOut();
}
