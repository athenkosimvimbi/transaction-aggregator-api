package com.mvimbi.aggregator.domain;

/**
 * Spending / income categories a transaction can be filed under.
 *
 * @author Athenkosi Mvimbi
 */
public enum Category {
    SALARY("Salary"),
    RENT("Rent"),
    FEES("Bank fees"),
    INSURANCE("Insurance"),
    UTILITIES("Utilities"),
    GROCERIES("Groceries"),
    FUEL("Fuel"),
    AIRTIME_DATA("Airtime & data"),
    DINING("Dining & takeaways"),
    TRANSPORT("Transport"),
    ENTERTAINMENT("Entertainment"),
    HEALTH("Health & pharmacy"),
    SHOPPING("Shopping"),
    TRANSFERS("Transfers"),
    OTHER("Other");

    private final String label;

    Category(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
