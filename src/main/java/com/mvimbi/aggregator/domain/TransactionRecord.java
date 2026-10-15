package com.mvimbi.aggregator.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entity representing a transaction imported from an external source system.
 *
 * Monetary amounts are stored as signed ZAR values, with positive amounts
 * representing credits and negative amounts representing debits.
 *
 * A direct customer identifier is used instead of a JPA entity association
 * to keep the model lightweight, reduce query complexity, and avoid
 * lazy-loading concerns when retrieving transaction data.
 *
 * @author Athenkosi Mvimbi
 */

@Entity
@Table(name = "transaction_record")
public class TransactionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SourceSystem source;

    @Column(name = "external_id", nullable = false, length = 64)
    private String externalId;

    @Column(name = "booked_on", nullable = false)
    private LocalDate bookedOn;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(nullable = false, length = 120)
    private String merchant;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(name = "categorised_by", nullable = false, length = 10)
    private CategorisedBy categorisedBy;

    protected TransactionRecord() {
        // required by JPA
    }

    public TransactionRecord(Long customerId, SourceSystem source, String externalId, LocalDate bookedOn,
                             String description, String merchant, BigDecimal amount, Category category) {
        this.customerId = customerId;
        this.source = source;
        this.externalId = externalId;
        this.bookedOn = bookedOn;
        this.description = description;
        this.merchant = merchant;
        this.amount = Money.zar(amount);
        this.category = category;
        this.categorisedBy = CategorisedBy.RULE;
    }

    /** Customer override of the category, locks it against the rules engine. */
    public void recategorise(Category newCategory) {
        this.category = newCategory;
        this.categorisedBy = CategorisedBy.MANUAL;
    }

    public TransactionType getType() {
        return amount.signum() > 0 ? TransactionType.CREDIT : TransactionType.DEBIT;
    }

    public Long getId() { return id; }
    public Long getCustomerId() { return customerId; }
    public SourceSystem getSource() { return source; }
    public String getExternalId() { return externalId; }
    public LocalDate getBookedOn() { return bookedOn; }
    public String getDescription() { return description; }
    public String getMerchant() { return merchant; }
    public BigDecimal getAmount() { return amount; }
    public Category getCategory() { return category; }
    public CategorisedBy getCategorisedBy() { return categorisedBy; }
}
