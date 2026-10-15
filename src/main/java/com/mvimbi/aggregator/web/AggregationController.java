package com.mvimbi.aggregator.web;

import com.mvimbi.aggregator.service.AggregationService;
import com.mvimbi.aggregator.web.dto.AggregationResponse;
import com.mvimbi.aggregator.web.dto.SummaryDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Aggregated views of a customer's money. Every endpoint accepts an optional
 * from / to date range (yyyy-MM-dd, both inclusive).
 *
 * @author Athenkosi Mvimbi
 */
@RestController
@RequestMapping("/api/v1/customers/{customerId}")
@Tag(name = "Aggregations")
public class AggregationController {

    private final AggregationService aggregations;

    public AggregationController(AggregationService aggregations) {
        this.aggregations = aggregations;
    }

    @GetMapping("/summary")
    @Operation(summary = "Total income, expenses, net cashflow and savings rate")
    public SummaryDto summary(@PathVariable Long customerId,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return aggregations.summary(customerId, from, to);
    }

    @GetMapping("/aggregations/by-category")
    @Operation(summary = "Money in / out per category, with share of total spend")
    public AggregationResponse byCategory(@PathVariable Long customerId,
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return aggregations.byCategory(customerId, from, to);
    }

    @GetMapping("/aggregations/by-month")
    @Operation(summary = "Monthly cashflow")
    public AggregationResponse byMonth(@PathVariable Long customerId,
                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return aggregations.byMonth(customerId, from, to);
    }

    @GetMapping("/aggregations/by-source")
    @Operation(summary = "Money in / out per data source (bank, card, wallet)")
    public AggregationResponse bySource(@PathVariable Long customerId,
                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return aggregations.bySource(customerId, from, to);
    }

    @GetMapping("/aggregations/top-merchants")
    @Operation(summary = "Merchants the customer spent the most with")
    public AggregationResponse topMerchants(@PathVariable Long customerId,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                            @RequestParam(defaultValue = "10") int limit) {
        return aggregations.topMerchants(customerId, from, to, limit);
    }
}
