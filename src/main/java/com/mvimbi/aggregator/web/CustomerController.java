package com.mvimbi.aggregator.web;

import com.mvimbi.aggregator.service.CustomerService;
import com.mvimbi.aggregator.web.dto.CustomerDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** @author Athenkosi Mvimbi */
@RestController
@RequestMapping("/api/v1/customers")
@Tag(name = "Customers")
public class CustomerController {

    private final CustomerService customers;

    public CustomerController(CustomerService customers) {
        this.customers = customers;
    }

    @GetMapping
    @Operation(summary = "List all customers")
    public List<CustomerDto> list() {
        return customers.list();
    }

    @GetMapping("/{customerId}")
    @Operation(summary = "Get one customer")
    public CustomerDto get(@PathVariable Long customerId) {
        return customers.get(customerId);
    }
}
