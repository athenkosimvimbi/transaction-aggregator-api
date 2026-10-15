package com.mvimbi.aggregator.domain;
/**
 * Indicates how a transaction category was assigned.
 * Categories may be assigned automatically by the rules engine
 * or manually by the customer. Categories marked as MANUAL
 * are excluded from any subsequent automated recategorisation.
 *
 * @author Athenkosi Mvimbi
 */

public enum CategorisedBy {
    RULE,
    MANUAL
}
