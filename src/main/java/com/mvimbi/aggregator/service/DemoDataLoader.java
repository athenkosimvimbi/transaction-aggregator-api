package com.mvimbi.aggregator.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Loads sample transaction data during application startup.
 *
 * When app.sync-on-startup=true, the loader synchronises the last six
 * months of transactions from the configured mock sources, ensuring
 * that the API and UI have data available immediately after startup.
 *
 * The synchronisation process is idempotent and can safely be executed
 * on every application boot without creating duplicate transactions.
 *
 * @author Athenkosi Mvimbi
 */

@Component
@ConditionalOnProperty(name = "app.sync-on-startup", havingValue = "true")
public class DemoDataLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataLoader.class);

    private final IngestionService ingestion;

    public DemoDataLoader(IngestionService ingestion) {
        this.ingestion = ingestion;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            var results = ingestion.syncAll(6);
            int inserted = results.stream().mapToInt(r -> r.inserted()).sum();
            log.info("Startup sync finished: {} new transactions from {} source runs", inserted, results.size());
        } catch (RuntimeException e) {
            // never stop the API from starting just because the demo load failed
            log.warn("Startup sync failed, continuing without demo data", e);
        }
    }
}
