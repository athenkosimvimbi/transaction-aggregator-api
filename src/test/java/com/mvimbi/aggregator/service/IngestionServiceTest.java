package com.mvimbi.aggregator.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mvimbi.aggregator.domain.Customer;
import com.mvimbi.aggregator.domain.SourceSystem;
import com.mvimbi.aggregator.repository.CustomerRepository;
import com.mvimbi.aggregator.repository.TransactionRepository;
import com.mvimbi.aggregator.source.RawTransaction;
import com.mvimbi.aggregator.source.TransactionSource;
import com.mvimbi.aggregator.web.InvalidRequestException;
import com.mvimbi.aggregator.web.dto.SyncResultDto;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link IngestionService}.
 *
 * Verifies transaction synchronisation behaviour using mocked collaborators,
 * allowing ingestion logic to be tested in isolation without requiring a
 * database, external source systems, or Spring context startup.
 *
 * @author Athenkosi Mvimbi
 */

class IngestionServiceTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-10-14T10:00:00Z"), ZoneId.of("Africa/Johannesburg"));
    private final TransactionRepository repo = mock(TransactionRepository.class);
    private final TransactionSource source = mock(TransactionSource.class);
    private final Customer customer = mock(Customer.class);

    private IngestionService service() {
        when(source.system()).thenReturn(SourceSystem.MOCK_BANK);
        when(customer.getId()).thenReturn(1L);
        when(customer.getExternalRef()).thenReturn("CUST-1001");
        return new IngestionService(List.of(source), repo, mock(CustomerService.class), mock(CustomerRepository.class),
                new CategorizationService(), clock, mock(PlatformTransactionManager.class));
    }

    private static RawTransaction raw(String id, String merchant, String amount) {
        return new RawTransaction(SourceSystem.MOCK_BANK, id, LocalDate.of(2026, 10, 1), merchant, "POS PURCHASE",
                new BigDecimal(amount));
    }

    @Test
    void storesNewRowsAndSkipsOnesWeAlreadyHave() {
        IngestionService service = service();
        when(source.fetch(any(), any(), any())).thenReturn(List.of(
                raw("A-1", "CHECKERS", "-100.00"),
                raw("A-2", "ENGEN", "-500.00")));
        when(repo.findExternalIds(any(), any(), any(), any())).thenReturn(List.of("A-1"));
        when(repo.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<SyncResultDto> results = service.syncCustomer(customer, 3);

        assertThat(results).hasSize(1);
        SyncResultDto r = results.get(0);
        assertThat(r.status()).isEqualTo("OK");
        assertThat(r.fetched()).isEqualTo(2);
        assertThat(r.inserted()).isEqualTo(1);
        assertThat(r.duplicates()).isEqualTo(1);
    }

    @Test
    void ignoresZeroAmountRowsAndRepeatsInsideOneBatch() {
        IngestionService service = service();
        when(source.fetch(any(), any(), any())).thenReturn(List.of(
                raw("B-1", "CHECKERS", "-100.00"),
                raw("B-1", "CHECKERS", "-100.00"),
                raw("B-2", "SHELL", "0.00")));
        when(repo.findExternalIds(any(), any(), any(), any())).thenReturn(List.of());
        when(repo.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        SyncResultDto r = service.syncCustomer(customer, 1).get(0);

        assertThat(r.inserted()).isEqualTo(1);
        assertThat(r.duplicates()).isEqualTo(2);
    }

    @Test
    void aFailingSourceIsReportedNotThrown() {
        IngestionService service = service();
        when(source.fetch(any(), any(), any())).thenThrow(new IllegalStateException("source is down"));

        SyncResultDto r = service.syncCustomer(customer, 1).get(0);

        assertThat(r.status()).isEqualTo("FAILED");
        assertThat(r.error()).contains("source is down");
        verify(repo, never()).saveAll(anyList());
    }

    @Test
    void rejectsAnOutOfRangeMonthCount() {
        IngestionService service = service();
        assertThatThrownBy(() -> service.syncCustomer(customer, 0)).isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> service.syncCustomer(customer, 25)).isInstanceOf(InvalidRequestException.class);
    }
}
