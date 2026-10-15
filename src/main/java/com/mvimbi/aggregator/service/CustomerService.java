package com.mvimbi.aggregator.service;

import com.mvimbi.aggregator.domain.Customer;
import com.mvimbi.aggregator.repository.CustomerRepository;
import com.mvimbi.aggregator.web.ResourceNotFoundException;
import com.mvimbi.aggregator.web.dto.CustomerDto;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** @author Athenkosi Mvimbi */
@Service
public class CustomerService {

    private final CustomerRepository customers;

    public CustomerService(CustomerRepository customers) {
        this.customers = customers;
    }

    @Transactional(readOnly = true)
    public List<CustomerDto> list() {
        return customers.findAll(Sort.by("id")).stream().map(CustomerDto::from).toList();
    }

    @Transactional(readOnly = true)
    public CustomerDto get(Long id) {
        return CustomerDto.from(require(id));
    }

    /** Used by the other services to fail fast with a 404 when the customer doesn't exist. */
    @Transactional(readOnly = true)
    public Customer require(Long id) {
        return customers.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer " + id + " not found"));
    }
}
