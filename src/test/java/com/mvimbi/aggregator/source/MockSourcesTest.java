package com.mvimbi.aggregator.source;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Validates the behaviour of the mock transaction sources.
 *
 * Ensures generated transaction data is deterministic, produces unique
 * external identifiers, respects the requested date range, and never
 * returns zero-value transactions. These guarantees allow the mock
 * sources to behave consistently as reliable test data providers.
 *
 * @author Athenkosi Mvimbi
 */

class MockSourcesTest {

    private static final LocalDate FROM = LocalDate.of(2026, 5, 1);
    private static final LocalDate TO = LocalDate.of(2026, 10, 14);

    private final List<TransactionSource> sources =
            List.of(new MockBankSource(), new MockCardSource(), new MockWalletSource());

    @Test
    void sameRequestGivesIdenticalResults() {
        for (TransactionSource s : sources) {
            assertThat(s.fetch("CUST-1001", FROM, TO)).isEqualTo(s.fetch("CUST-1001", FROM, TO));
        }
    }

    @Test
    void externalIdsAreUniquePerSource() {
        for (TransactionSource s : sources) {
            List<RawTransaction> rows = s.fetch("CUST-1002", FROM, TO);
            assertThat(rows).isNotEmpty();
            assertThat(new HashSet<>(rows.stream().map(RawTransaction::externalId).toList())).hasSameSizeAs(rows);
        }
    }

    @Test
    void rowsStayInsideTheRequestedWindowAndAreNeverZero() {
        for (TransactionSource s : sources) {
            for (RawTransaction r : s.fetch("CUST-1003", FROM, TO)) {
                assertThat(r.bookedOn()).isBetween(FROM, TO);
                assertThat(r.amount().signum()).isNotZero();
                assertThat(r.amount().scale()).isEqualTo(2);
                assertThat(r.source()).isEqualTo(s.system());
            }
        }
    }

    @Test
    void narrowerWindowReturnsASubsetWithTheSameIds() {
        TransactionSource bank = new MockBankSource();
        var all = bank.fetch("CUST-1001", FROM, TO).stream().map(RawTransaction::externalId).toList();
        var some = bank.fetch("CUST-1001", LocalDate.of(2026, 8, 1), TO).stream().map(RawTransaction::externalId).toList();
        assertThat(all).containsAll(some);
    }
}
