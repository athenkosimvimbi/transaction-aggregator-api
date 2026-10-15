package com.mvimbi.aggregator.domain;

/**
 * Represents the systems that provide transaction data.
 *
 * In the current implementation these are mock source systems used to
 * simulate transaction feeds from bank accounts, credit cards, and
 * mobile wallet platforms.
 *
 * @author Athenkosi Mvimbi
 */

public enum SourceSystem {
    MOCK_BANK("Bank account"),
    MOCK_CARD("Credit card"),
    MOCK_WALLET("Mobile wallet");

    private final String label;

    SourceSystem(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
