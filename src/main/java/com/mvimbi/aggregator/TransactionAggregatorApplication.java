package com.mvimbi.aggregator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Transaction Aggregation API.
 *
 * @author Athenkosi Mvimbi
 */
@SpringBootApplication
public class TransactionAggregatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransactionAggregatorApplication.class, args);
    }
}
