package com.mvimbi.aggregator.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Represents a bank customer within the system.
 * The externalRef field contains the unique customer identifier
 * used by external mock data sources when supplying transaction data.
 *
 * @author Athenkosi Mvimbi
 */

@Entity
@Table(name = "customer")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_ref", nullable = false, unique = true, length = 40)
    private String externalRef;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    protected Customer() {
        // required by JPA
    }

    public Customer(String externalRef, String fullName) {
        this.externalRef = externalRef;
        this.fullName = fullName;
    }

    public Long getId() {
        return id;
    }

    public String getExternalRef() {
        return externalRef;
    }

    public String getFullName() {
        return fullName;
    }
}
