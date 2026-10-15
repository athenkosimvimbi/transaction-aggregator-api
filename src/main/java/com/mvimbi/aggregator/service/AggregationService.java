package com.mvimbi.aggregator.service;

import com.mvimbi.aggregator.domain.Category;
import com.mvimbi.aggregator.domain.Money;
import com.mvimbi.aggregator.domain.SourceSystem;
import com.mvimbi.aggregator.repository.AggregateView;
import com.mvimbi.aggregator.repository.TransactionRepository;
import com.mvimbi.aggregator.web.InvalidRequestException;
import com.mvimbi.aggregator.web.dto.AggregateDto;
import com.mvimbi.aggregator.web.dto.AggregationResponse;
import com.mvimbi.aggregator.web.dto.SummaryDto;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.function.UnaryOperator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for generating transaction summaries and
 * aggregated views across categories, source systems, merchants,
 * and time periods.
 *
 * Aggregation results returned by the repository are transformed
 * into API response DTOs enriched with calculated metrics such as
 * net values, spending share percentages, and savings rates.
 *
 * As the application is ZAR-only, all monetary calculations are
 * performed without currency conversion concerns.
 *
 * @author Athenkosi Mvimbi
 */

@Service
public class AggregationService {

    // wide defaults so the SQL never has to deal with null dates
    private static final LocalDate MIN_DATE = LocalDate.of(1970, 1, 1);
    private static final LocalDate MAX_DATE = LocalDate.of(2999, 12, 31);
    private static final int MAX_TOP_LIMIT = 50;

    private final TransactionRepository transactions;
    private final CustomerService customers;

    public AggregationService(TransactionRepository transactions, CustomerService customers) {
        this.transactions = transactions;
        this.customers = customers;
    }

    @Transactional(readOnly = true)
    public SummaryDto summary(Long customerId, LocalDate from, LocalDate to) {
        customers.require(customerId);
        validateRange(from, to);

        AggregateView row = transactions.summarise(customerId, lower(from), upper(to)).get(0);
        BigDecimal income = Money.zar(row.getTotalIn());
        BigDecimal expenses = Money.zar(row.getTotalOut());
        BigDecimal net = income.subtract(expenses);

        // savings rate = how much of the income was kept, undefined when there was no income
        BigDecimal savingsRate = income.signum() == 0
                ? null
                : net.multiply(BigDecimal.valueOf(100)).divide(income, 1, RoundingMode.HALF_EVEN);

        return new SummaryDto(customerId, Money.CURRENCY, from, to, row.getCnt(), income, expenses, net, savingsRate);
    }

    @Transactional(readOnly = true)
    public AggregationResponse byCategory(Long customerId, LocalDate from, LocalDate to) {
        customers.require(customerId);
        validateRange(from, to);
        List<AggregateView> rows = transactions.byCategory(customerId, lower(from), upper(to));
        return response(customerId, "category", from, to, rows, key -> Category.valueOf(key).getLabel());
    }

    @Transactional(readOnly = true)
    public AggregationResponse byMonth(Long customerId, LocalDate from, LocalDate to) {
        customers.require(customerId);
        validateRange(from, to);
        List<AggregateView> rows = transactions.byMonth(customerId, lower(from), upper(to));
        return response(customerId, "month", from, to, rows, UnaryOperator.identity());
    }

    @Transactional(readOnly = true)
    public AggregationResponse bySource(Long customerId, LocalDate from, LocalDate to) {
        customers.require(customerId);
        validateRange(from, to);
        List<AggregateView> rows = transactions.bySource(customerId, lower(from), upper(to));
        return response(customerId, "source", from, to, rows, key -> SourceSystem.valueOf(key).getLabel());
    }

    @Transactional(readOnly = true)
    public AggregationResponse topMerchants(Long customerId, LocalDate from, LocalDate to, int limit) {
        customers.require(customerId);
        validateRange(from, to);
        if (limit < 1 || limit > MAX_TOP_LIMIT) {
            throw new InvalidRequestException("'limit' must be between 1 and " + MAX_TOP_LIMIT);
        }
        List<AggregateView> rows = transactions.topMerchants(customerId, lower(from), upper(to), limit);
        return response(customerId, "merchant", from, to, rows, UnaryOperator.identity());
    }

    // ---- helpers ----

    private AggregationResponse response(Long customerId, String groupBy, LocalDate from, LocalDate to,
                                         List<AggregateView> rows, UnaryOperator<String> labeller) {
        BigDecimal totalSpend = rows.stream()
                .map(r -> Money.zar(r.getTotalOut()))
                .reduce(Money.zero(), BigDecimal::add);

        List<AggregateDto> dtos = rows.stream().map(r -> {
            BigDecimal in = Money.zar(r.getTotalIn());
            BigDecimal out = Money.zar(r.getTotalOut());
            BigDecimal share = totalSpend.signum() == 0
                    ? BigDecimal.ZERO.setScale(1)
                    : out.multiply(BigDecimal.valueOf(100)).divide(totalSpend, 1, RoundingMode.HALF_EVEN);
            return new AggregateDto(r.getLabel(), labeller.apply(r.getLabel()), r.getCnt(), in, out,
                    in.subtract(out), share);
        }).toList();

        return new AggregationResponse(customerId, Money.CURRENCY, groupBy, from, to, dtos);
    }

    private static void validateRange(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new InvalidRequestException("'from' must not be after 'to'");
        }
    }

    private static LocalDate lower(LocalDate from) {
        return from == null ? MIN_DATE : from;
    }

    private static LocalDate upper(LocalDate to) {
        return to == null ? MAX_DATE : to;
    }
}
