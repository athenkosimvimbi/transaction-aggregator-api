package com.mvimbi.aggregator.web.dto;

import com.mvimbi.aggregator.domain.Customer;

/** @author Athenkosi Mvimbi */
public record CustomerDto(Long id, String reference, String fullName) {

    public static CustomerDto from(Customer c) {
        return new CustomerDto(c.getId(), c.getExternalRef(), c.getFullName());
    }
}
