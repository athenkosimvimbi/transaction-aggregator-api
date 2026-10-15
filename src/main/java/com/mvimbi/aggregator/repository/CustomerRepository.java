package com.mvimbi.aggregator.repository;

import com.mvimbi.aggregator.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Athenkosi Mvimbi
 */
public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
