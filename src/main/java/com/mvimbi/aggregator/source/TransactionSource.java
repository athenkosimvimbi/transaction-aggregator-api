package com.mvimbi.aggregator.source;

import com.mvimbi.aggregator.domain.SourceSystem;
import java.time.LocalDate;
import java.util.List;

/**
 * Contract every data source implements. A real integration (open banking feed,
 * card processor, etc.) would implement this too, so the rest of the app never
 * needs to know where the data came from.
 *
 * @author Athenkosi Mvimbi
 */
public interface TransactionSource {

    SourceSystem system();

    /** Returns every transaction for the customer between from and to (both inclusive). */
    List<RawTransaction> fetch(String customerRef, LocalDate from, LocalDate to);
}
