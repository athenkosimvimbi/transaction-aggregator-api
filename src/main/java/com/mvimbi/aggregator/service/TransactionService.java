package com.mvimbi.aggregator.service;

import com.mvimbi.aggregator.domain.Category;
import com.mvimbi.aggregator.domain.TransactionRecord;
import com.mvimbi.aggregator.repository.TransactionRepository;
import com.mvimbi.aggregator.web.ResourceNotFoundException;
import com.mvimbi.aggregator.web.dto.PageDto;
import com.mvimbi.aggregator.web.dto.TransactionDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** @author Athenkosi Mvimbi */
@Service
public class TransactionService {

    private final TransactionRepository transactions;
    private final CustomerService customers;

    public TransactionService(TransactionRepository transactions, CustomerService customers) {
        this.transactions = transactions;
        this.customers = customers;
    }

    @Transactional(readOnly = true)
    public PageDto<TransactionDto> search(Long customerId, TransactionFilter filter, Pageable pageable) {
        customers.require(customerId);
        filter.validate();
        Page<TransactionRecord> page = transactions.findAll(
                TransactionSpecifications.withFilters(customerId, filter), pageable);
        return PageDto.from(page.map(TransactionDto::from));
    }

    @Transactional(readOnly = true)
    public TransactionDto get(Long id) {
        return TransactionDto.from(find(id));
    }

    /** Lets the customer correct a category. The row is then locked as MANUAL so a re-sync can't undo it. */
    @Transactional
    public TransactionDto recategorise(Long id, Category category) {
        TransactionRecord record = find(id);
        record.recategorise(category); // JPA dirty checking saves it when the transaction commits
        return TransactionDto.from(record);
    }

    private TransactionRecord find(Long id) {
        return transactions.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction " + id + " not found"));
    }
}
