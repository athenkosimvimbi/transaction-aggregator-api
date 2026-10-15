package com.mvimbi.aggregator.source;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Shared plumbing for the mock sources.
 *
 * The data is generated per customer and per month from a seeded Random, so the same
 * request always gives the same transactions with the same external ids. That is what
 * lets the sync be safely re-run without creating duplicates.
 *
 * @author Athenkosi Mvimbi
 */
public abstract class AbstractMockSource implements TransactionSource {

    /** A transaction before it has a date and id. */
    protected record Draft(int dayOfMonth, String merchant, String description, BigDecimal amount) {
    }

    /** Builds the list of transactions for a single month. Order must stay stable (it drives the ids). */
    protected abstract List<Draft> generateMonth(String customerRef, Random rnd);

    @Override
    public List<RawTransaction> fetch(String customerRef, LocalDate from, LocalDate to) {
        List<RawTransaction> result = new ArrayList<>();
        YearMonth last = YearMonth.from(to);

        for (YearMonth ym = YearMonth.from(from); !ym.isAfter(last); ym = ym.plusMonths(1)) {
            Random rnd = new Random(Objects.hash(customerRef, system().name(), ym.toString()));
            int index = 0;
            for (Draft d : generateMonth(customerRef, rnd)) {
                index++;
                // clamp the day so e.g. day 30 in February still works
                LocalDate date = ym.atDay(Math.min(d.dayOfMonth(), ym.lengthOfMonth()));
                if (date.isBefore(from) || date.isAfter(to)) {
                    continue;
                }
                String externalId = system().name() + "-" + customerRef + "-" + ym + "-" + index;
                result.add(new RawTransaction(system(), externalId, date, d.merchant(), d.description(), d.amount()));
            }
        }
        return result;
    }

    // ---- small helpers the concrete sources share ----

    /** Random rand amount between min and max (inclusive), with cents. */
    protected static BigDecimal rand(Random rnd, int minRand, int maxRand) {
        long cents = minRand * 100L + rnd.nextInt((maxRand - minRand) * 100 + 1);
        return BigDecimal.valueOf(cents, 2);
    }

    protected static BigDecimal debit(BigDecimal amount) {
        return amount.negate();
    }

    protected static String pick(Random rnd, String... options) {
        return options[rnd.nextInt(options.length)];
    }

    protected static int day(Random rnd, int min, int max) {
        return min + rnd.nextInt(max - min + 1);
    }

    /** Every customer gets a stable, believable monthly salary between R18 000 and R42 000. */
    protected static int baseSalary(String customerRef) {
        return 18_000 + Math.floorMod(customerRef.hashCode(), 25) * 1_000;
    }
}
