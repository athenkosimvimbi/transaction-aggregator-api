package com.mvimbi.aggregator.web.dto;

import com.mvimbi.aggregator.domain.SourceSystem;

/** Outcome of pulling one source for one customer. @author Athenkosi Mvimbi */
public record SyncResultDto(
        Long customerId,
        SourceSystem source,
        int fetched,
        int inserted,
        int duplicates,
        String status,
        String error) {

    public static SyncResultDto ok(Long customerId, SourceSystem source, int fetched, int inserted, int duplicates) {
        return new SyncResultDto(customerId, source, fetched, inserted, duplicates, "OK", null);
    }

    public static SyncResultDto failed(Long customerId, SourceSystem source, String error) {
        return new SyncResultDto(customerId, source, 0, 0, 0, "FAILED", error);
    }
}
