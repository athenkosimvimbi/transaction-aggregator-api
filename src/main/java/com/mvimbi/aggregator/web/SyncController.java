package com.mvimbi.aggregator.web;

import com.mvimbi.aggregator.service.IngestionService;
import com.mvimbi.aggregator.web.dto.SyncResultDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** @author Athenkosi Mvimbi */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Sync")
public class SyncController {

    private final IngestionService ingestion;

    public SyncController(IngestionService ingestion) {
        this.ingestion = ingestion;
    }

    @PostMapping("/sync")
    @Operation(summary = "Pull the last N months from every source for every customer")
    public List<SyncResultDto> syncAll(@RequestParam(defaultValue = "6") int months) {
        return ingestion.syncAll(months);
    }

    @PostMapping("/customers/{customerId}/sync")
    @Operation(summary = "Pull the last N months from every source for one customer")
    public List<SyncResultDto> syncCustomer(@PathVariable Long customerId,
                                            @RequestParam(defaultValue = "6") int months) {
        return ingestion.syncCustomer(customerId, months);
    }
}
