package com.mvimbi.aggregator.web.dto;

import com.mvimbi.aggregator.domain.Category;
import jakarta.validation.constraints.NotNull;

/** @author Athenkosi Mvimbi */
public record RecategoriseRequest(@NotNull Category category) {
}
