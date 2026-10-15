package com.mvimbi.aggregator.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.mvimbi.aggregator.domain.Category;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Unit tests for the rules engine. No Spring or database needed.
 *
 * @author Athenkosi Mvimbi
 */
class CategorizationServiceTest {

    private final CategorizationService service = new CategorizationService();

    @ParameterizedTest(name = "{0} / {1} / {2} -> {3}")
    @CsvSource(delimiter = '|', value = {
            "CHECKERS      | POS PURCHASE CHECKERS     | -250.00 | GROCERIES",
            "ENGEN         | FUEL PURCHASE             | -600.00 | FUEL",
            "ESKOM         | PREPAID ELECTRICITY       | -500.00 | UTILITIES",
            "VODACOM       | AIRTIME TOPUP             | -99.00  | AIRTIME_DATA",
            "UBER EATS     | CARD PURCHASE             | -140.00 | DINING",
            "UBER          | RIDE                      | -85.00  | TRANSPORT",
            "NETFLIX       | SUBSCRIPTION              | -199.00 | ENTERTAINMENT",
            "DIS-CHEM      | PHARMACY PURCHASE         | -120.00 | HEALTH",
            "TAKEALOT      | ONLINE PURCHASE           | -999.00 | SHOPPING",
            "LANDLORD EFT  | RENT PAYMENT              | -7000.00| RENT",
            "BANK          | MONTHLY ACCOUNT FEE       | -69.50  | FEES",
            "DISCOVERY INSURE | INSURANCE PREMIUM      | -900.00 | INSURANCE",
            "SAVINGS ACCOUNT | EFT TRANSFER TO SAVINGS | -1000.00| TRANSFERS",
            "EMPLOYER PAYROLL | SALARY PAYMENT         | 25000.00| SALARY",
            "SOME RANDOM SHOP | CARD PURCHASE          | -10.00  | OTHER"
    })
    void picksTheExpectedCategory(String merchant, String description, String amount, Category expected) {
        assertThat(service.categorise(merchant, description, new BigDecimal(amount))).isEqualTo(expected);
    }

    @Test
    void salaryKeywordOnADebitIsNotIncome() {
        Category result = service.categorise("EMPLOYER PAYROLL", "SALARY REVERSAL", new BigDecimal("-100.00"));
        assertThat(result).isNotEqualTo(Category.SALARY);
    }

    @Test
    void keywordsMatchWholeWordsOnly() {
        // "BP" must not match inside "BPX" or "ABP"
        assertThat(service.categorise("ABPX TRADING", "CARD PURCHASE", new BigDecimal("-50.00")))
                .isEqualTo(Category.OTHER);
        assertThat(service.categorise("BP", "CARD PURCHASE", new BigDecimal("-50.00")))
                .isEqualTo(Category.FUEL);
    }

    @Test
    void handlesNullsWithoutThrowing() {
        assertThat(service.categorise(null, null, new BigDecimal("-1.00"))).isEqualTo(Category.OTHER);
    }
}
