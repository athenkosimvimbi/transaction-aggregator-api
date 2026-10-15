package com.mvimbi.aggregator.web;

import com.mvimbi.aggregator.domain.Category;
import com.mvimbi.aggregator.domain.SourceSystem;
import com.mvimbi.aggregator.web.dto.LookupDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Lists of valid category / source codes, handy for building dropdowns. @author Athenkosi Mvimbi */

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Reference data")
public class ReferenceDataController {

    @GetMapping("/categories")
    @Operation(summary = "All transaction categories")
    public List<LookupDto> categories() {
        return Arrays.stream(Category.values()).map(c -> new LookupDto(c.name(), c.getLabel())).toList();
    }

    @GetMapping("/sources")
    @Operation(summary = "All data sources")
    public List<LookupDto> sources() {
        return Arrays.stream(SourceSystem.values()).map(s -> new LookupDto(s.name(), s.getLabel())).toList();
    }
}
