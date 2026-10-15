package com.mvimbi.aggregator.service;

import com.mvimbi.aggregator.domain.TransactionRecord;
import com.mvimbi.aggregator.domain.TransactionType;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

/**
 * Builds dynamic transaction search criteria using the JPA Criteria API.
 *
 * Filters are applied only when supplied, allowing a single query
 * definition to support multiple search combinations without generating
 * custom SQL for each scenario.
 *
 * All filter values are bound as query parameters rather than being
 * concatenated into SQL, ensuring safe query construction and protection
 * against SQL injection.
 *
 * @author Athenkosi Mvimbi
 */

public final class TransactionSpecifications {

    private TransactionSpecifications() {
    }

    public static Specification<TransactionRecord> withFilters(Long customerId, TransactionFilter f) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("customerId"), customerId));

            if (f.from() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.<LocalDate>get("bookedOn"), f.from()));
            }
            if (f.to() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.<LocalDate>get("bookedOn"), f.to()));
            }
            if (f.category() != null) {
                predicates.add(cb.equal(root.get("category"), f.category()));
            }
            if (f.source() != null) {
                predicates.add(cb.equal(root.get("source"), f.source()));
            }
            if (f.type() == TransactionType.CREDIT) {
                predicates.add(cb.greaterThan(root.<BigDecimal>get("amount"), BigDecimal.ZERO));
            } else if (f.type() == TransactionType.DEBIT) {
                predicates.add(cb.lessThan(root.<BigDecimal>get("amount"), BigDecimal.ZERO));
            }
            if (f.minAmount() != null) {
                predicates.add(cb.greaterThanOrEqualTo(cb.abs(root.<BigDecimal>get("amount")), f.minAmount()));
            }
            if (f.maxAmount() != null) {
                predicates.add(cb.lessThanOrEqualTo(cb.abs(root.<BigDecimal>get("amount")), f.maxAmount()));
            }
            if (f.q() != null && !f.q().isBlank()) {
                String pattern = likePattern(f.q());
                predicates.add(cb.or(
                        cb.like(cb.lower(root.<String>get("merchant")), pattern, '\\'),
                        cb.like(cb.lower(root.<String>get("description")), pattern, '\\')));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /** Escapes % and _ so a search for "50%" is treated literally. */
    private static String likePattern(String q) {
        String escaped = q.trim().toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }
}
