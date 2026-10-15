package com.mvimbi.aggregator.repository;

import com.mvimbi.aggregator.domain.SourceSystem;
import com.mvimbi.aggregator.domain.TransactionRecord;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for managing transaction records and supporting transaction-based
 * reporting and aggregation operations.
 *
 * Standard transaction lookups are implemented using JPA repositories and
 * Specifications to provide flexible filtering capabilities. Aggregation
 * queries are implemented using native SQL to leverage database-side grouping
 * and summation, thereby reducing application memory usage and improving
 * performance for reporting operations.
 *
 * Date range parameters supplied to aggregation methods are expected to be
 * non-null. Default date boundaries are applied by the service layer where
 * required.
 *
 * @author Athenkosi Mvimbi
 */

public interface TransactionRepository
        extends JpaRepository<TransactionRecord, Long>, JpaSpecificationExecutor<TransactionRecord> {

    @Query("""
            select t.externalId from TransactionRecord t
            where t.customerId = :customerId and t.source = :source
              and t.bookedOn between :fromDate and :toDate
            """)
    List<String> findExternalIds(@Param("customerId") Long customerId,
                                 @Param("source") SourceSystem source,
                                 @Param("fromDate") LocalDate fromDate,
                                 @Param("toDate") LocalDate toDate);

    @Query(value = """
            SELECT 'all' AS label, COUNT(*) AS cnt,
                   COALESCE(SUM(CASE WHEN amount > 0 THEN amount ELSE 0 END), 0) AS totalIn,
                   COALESCE(SUM(CASE WHEN amount < 0 THEN -amount ELSE 0 END), 0) AS totalOut
            FROM transaction_record
            WHERE customer_id = :customerId AND booked_on BETWEEN :fromDate AND :toDate
            """, nativeQuery = true)
    List<AggregateView> summarise(@Param("customerId") Long customerId,
                                  @Param("fromDate") LocalDate fromDate,
                                  @Param("toDate") LocalDate toDate);

    @Query(value = """
            SELECT category AS label, COUNT(*) AS cnt,
                   COALESCE(SUM(CASE WHEN amount > 0 THEN amount ELSE 0 END), 0) AS totalIn,
                   COALESCE(SUM(CASE WHEN amount < 0 THEN -amount ELSE 0 END), 0) AS totalOut
            FROM transaction_record
            WHERE customer_id = :customerId AND booked_on BETWEEN :fromDate AND :toDate
            GROUP BY category
            ORDER BY totalOut DESC, totalIn DESC
            """, nativeQuery = true)
    List<AggregateView> byCategory(@Param("customerId") Long customerId,
                                   @Param("fromDate") LocalDate fromDate,
                                   @Param("toDate") LocalDate toDate);

    @Query(value = """
            SELECT DATE_FORMAT(booked_on, '%Y-%m') AS label, COUNT(*) AS cnt,
                   COALESCE(SUM(CASE WHEN amount > 0 THEN amount ELSE 0 END), 0) AS totalIn,
                   COALESCE(SUM(CASE WHEN amount < 0 THEN -amount ELSE 0 END), 0) AS totalOut
            FROM transaction_record
            WHERE customer_id = :customerId AND booked_on BETWEEN :fromDate AND :toDate
            GROUP BY label
            ORDER BY label
            """, nativeQuery = true)
    List<AggregateView> byMonth(@Param("customerId") Long customerId,
                                @Param("fromDate") LocalDate fromDate,
                                @Param("toDate") LocalDate toDate);

    @Query(value = """
            SELECT source AS label, COUNT(*) AS cnt,
                   COALESCE(SUM(CASE WHEN amount > 0 THEN amount ELSE 0 END), 0) AS totalIn,
                   COALESCE(SUM(CASE WHEN amount < 0 THEN -amount ELSE 0 END), 0) AS totalOut
            FROM transaction_record
            WHERE customer_id = :customerId AND booked_on BETWEEN :fromDate AND :toDate
            GROUP BY source
            ORDER BY totalOut DESC
            """, nativeQuery = true)
    List<AggregateView> bySource(@Param("customerId") Long customerId,
                                 @Param("fromDate") LocalDate fromDate,
                                 @Param("toDate") LocalDate toDate);

    @Query(value = """
            SELECT merchant AS label, COUNT(*) AS cnt,
                   0 AS totalIn,
                   COALESCE(SUM(-amount), 0) AS totalOut
            FROM transaction_record
            WHERE customer_id = :customerId AND amount < 0 AND booked_on BETWEEN :fromDate AND :toDate
            GROUP BY merchant
            ORDER BY totalOut DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<AggregateView> topMerchants(@Param("customerId") Long customerId,
                                     @Param("fromDate") LocalDate fromDate,
                                     @Param("toDate") LocalDate toDate,
                                     @Param("limit") int limit);
}
