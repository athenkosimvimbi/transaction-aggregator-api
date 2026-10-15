package com.mvimbi.aggregator.source;

import com.mvimbi.aggregator.domain.SourceSystem;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A transaction exactly as a source system hands it to us, before categorising.
 * Amount is signed ZAR (credit positive, debit negative).
 *
 * @author Athenkosi Mvimbi
 */
public record RawTransaction(
        SourceSystem source,
        String externalId,
        LocalDate bookedOn,
        String merchant,
        String description,
        BigDecimal amount) {
}
