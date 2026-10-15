package com.mvimbi.aggregator.web;

import com.mvimbi.aggregator.domain.Category;
import com.mvimbi.aggregator.domain.SourceSystem;
import com.mvimbi.aggregator.domain.TransactionType;
import com.mvimbi.aggregator.service.TransactionFilter;
import com.mvimbi.aggregator.service.TransactionService;
import com.mvimbi.aggregator.web.dto.PageDto;
import com.mvimbi.aggregator.web.dto.RecategoriseRequest;
import com.mvimbi.aggregator.web.dto.TransactionDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** @author Athenkosi Mvimbi */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Transactions")
public class TransactionController {

    // only these fields can be sorted on, so callers can't make us sort by arbitrary columns
    private static final Set<String> SORTABLE = Set.of("bookedOn", "amount", "merchant", "category");
    private static final int MAX_PAGE_SIZE = 200;

    private final TransactionService transactions;

    public TransactionController(TransactionService transactions) {
        this.transactions = transactions;
    }

    @GetMapping("/customers/{customerId}/transactions")
    @Operation(summary = "Search a customer's transactions (filters, sorting, paging)")
    public PageDto<TransactionDto> search(
            @PathVariable Long customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) SourceSystem source,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(defaultValue = "bookedOn") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {

        if (page < 0) {
            throw new InvalidRequestException("'page' must be 0 or more");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new InvalidRequestException("'size' must be between 1 and " + MAX_PAGE_SIZE);
        }
        if (!SORTABLE.contains(sortBy)) {
            throw new InvalidRequestException("'sortBy' must be one of " + SORTABLE);
        }

        // id as a tie-breaker keeps paging stable when many rows share the same date/amount
        Sort sort = Sort.by(direction, sortBy).and(Sort.by(Sort.Direction.DESC, "id"));
        TransactionFilter filter = new TransactionFilter(from, to, category, source, type, minAmount, maxAmount, q);
        return transactions.search(customerId, filter, PageRequest.of(page, size, sort));
    }

    @GetMapping("/transactions/{id}")
    @Operation(summary = "Get one transaction")
    public TransactionDto get(@PathVariable Long id) {
        return transactions.get(id);
    }

    @PatchMapping("/transactions/{id}/category")
    @Operation(summary = "Correct the category of a transaction (locks it against the rules engine)")
    public TransactionDto recategorise(@PathVariable Long id, @Valid @RequestBody RecategoriseRequest body) {
        return transactions.recategorise(id, body.category());
    }
}
