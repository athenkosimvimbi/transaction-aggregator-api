package com.mvimbi.aggregator.service;

import com.mvimbi.aggregator.domain.Customer;
import com.mvimbi.aggregator.domain.Money;
import com.mvimbi.aggregator.domain.TransactionRecord;
import com.mvimbi.aggregator.repository.CustomerRepository;
import com.mvimbi.aggregator.repository.TransactionRepository;
import com.mvimbi.aggregator.source.RawTransaction;
import com.mvimbi.aggregator.source.TransactionSource;
import com.mvimbi.aggregator.web.InvalidRequestException;
import com.mvimbi.aggregator.web.dto.SyncResultDto;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Coordinates transaction ingestion from all configured source systems.
 *
 * Transactions retrieved from each source are categorised, validated,
 * and persisted to the database. Each source is processed within its
 * own transaction boundary so that a failure in one source does not
 * prevent successful ingestion from others.
 *
 * The ingestion process is idempotent. Transactions that already exist
 * for the same source and external identifier are detected and skipped,
 * making repeated synchronisation safe.
 *
 * @author Athenkosi Mvimbi
 */

@Service
public class IngestionService {

    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);
    private static final int MAX_MONTHS = 24;

    private final List<TransactionSource> sources;
    private final TransactionRepository transactions;
    private final CustomerService customerService;
    private final CustomerRepository customerRepository;
    private final CategorizationService categoriser;
    private final Clock clock;
    private final TransactionTemplate tx;

    public IngestionService(List<TransactionSource> sources,
                            TransactionRepository transactions,
                            CustomerService customerService,
                            CustomerRepository customerRepository,
                            CategorizationService categoriser,
                            Clock clock,
                            PlatformTransactionManager transactionManager) {
        this.sources = sources;
        this.transactions = transactions;
        this.customerService = customerService;
        this.customerRepository = customerRepository;
        this.categoriser = categoriser;
        this.clock = clock;
        this.tx = new TransactionTemplate(transactionManager);
    }

    public List<SyncResultDto> syncCustomer(Long customerId, int months) {
        return syncCustomer(customerService.require(customerId), months);
    }

    public List<SyncResultDto> syncCustomer(Customer customer, int months) {
        if (months < 1 || months > MAX_MONTHS) {
            throw new InvalidRequestException("'months' must be between 1 and " + MAX_MONTHS);
        }
        LocalDate to = LocalDate.now(clock);
        LocalDate from = to.minusMonths(months).withDayOfMonth(1);

        List<SyncResultDto> results = new ArrayList<>();
        for (TransactionSource source : sources) {
            results.add(syncOne(customer, source, from, to));
        }
        return results;
    }

    public List<SyncResultDto> syncAll(int months) {
        List<SyncResultDto> results = new ArrayList<>();
        for (Customer customer : customerRepository.findAll(Sort.by("id"))) {
            results.addAll(syncCustomer(customer, months));
        }
        return results;
    }

    private SyncResultDto syncOne(Customer customer, TransactionSource source, LocalDate from, LocalDate to) {
        try {
            List<RawTransaction> raw = source.fetch(customer.getExternalRef(), from, to);
            return tx.execute(status -> persist(customer, source, raw, from, to));
        } catch (RuntimeException e) {
            log.error("Sync failed for customer {} source {}", customer.getId(), source.system(), e);
            return SyncResultDto.failed(customer.getId(), source.system(), e.getMessage());
        }
    }

    private SyncResultDto persist(Customer customer, TransactionSource source, List<RawTransaction> raw,
                                  LocalDate from, LocalDate to) {
        Set<String> seen = new HashSet<>(transactions.findExternalIds(customer.getId(), source.system(), from, to));
        List<TransactionRecord> toSave = new ArrayList<>();
        int duplicates = 0;

        for (RawTransaction r : raw) {
            // skip bad rows from the source and anything we've already stored (or seen twice in this batch)
            if (r.amount() == null || r.amount().signum() == 0 || !seen.add(r.externalId())) {
                duplicates++;
                continue;
            }
            toSave.add(new TransactionRecord(customer.getId(), r.source(), r.externalId(), r.bookedOn(),
                    r.description(), r.merchant(), Money.zar(r.amount()),
                    categoriser.categorise(r.merchant(), r.description(), r.amount())));
        }

        if (!toSave.isEmpty()) {
            transactions.saveAll(toSave);
        }
        return SyncResultDto.ok(customer.getId(), source.system(), raw.size(), toSave.size(), duplicates);
    }
}
