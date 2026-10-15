package com.mvimbi.aggregator.web.dto;

import java.time.LocalDate;
import java.util.List;

/** @author Athenkosi Mvimbi */
public record AggregationResponse(
        Long customerId,
        String currency,
        String groupBy,
        LocalDate from,
        LocalDate to,
        List<AggregateDto> rows) {
}
