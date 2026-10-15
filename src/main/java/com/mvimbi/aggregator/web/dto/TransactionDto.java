package com.mvimbi.aggregator.web.dto;

import com.mvimbi.aggregator.domain.CategorisedBy;
import com.mvimbi.aggregator.domain.Category;
import com.mvimbi.aggregator.domain.Money;
import com.mvimbi.aggregator.domain.SourceSystem;
import com.mvimbi.aggregator.domain.TransactionRecord;
import com.mvimbi.aggregator.domain.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;

/** @author Athenkosi Mvimbi */
public record TransactionDto(
        Long id,
        Long customerId,
        SourceSystem source,
        String externalId,
        LocalDate bookedOn,
        String merchant,
        String description,
        BigDecimal amount,
        String currency,
        TransactionType type,
        Category category,
        String categoryLabel,
        CategorisedBy categorisedBy) {

    public static TransactionDto from(TransactionRecord t) {
        return new TransactionDto(t.getId(), t.getCustomerId(), t.getSource(), t.getExternalId(), t.getBookedOn(),
                t.getMerchant(), t.getDescription(), Money.zar(t.getAmount()), Money.CURRENCY, t.getType(),
                t.getCategory(), t.getCategory().getLabel(), t.getCategorisedBy());
    }
}
