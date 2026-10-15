package com.mvimbi.aggregator.web.dto;

import java.util.List;
import org.springframework.data.domain.Page;

/** Our own page shape so the JSON stays stable even if Spring changes its Page serialisation. @author Athenkosi Mvimbi */
public record PageDto<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public static <T> PageDto<T> from(Page<T> p) {
        return new PageDto<>(p.getContent(), p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());
    }
}
