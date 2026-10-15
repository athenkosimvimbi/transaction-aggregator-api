package com.mvimbi.aggregator.service;

import com.mvimbi.aggregator.domain.Category;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

/**
 * Rule-based transaction categorisation service.
 *
 * Transactions are categorised by evaluating the merchant name and
 * description against an ordered set of matching rules. The first
 * matching rule determines the assigned category, making rule order
 * significant (for example, "UBER EATS" must be matched before "UBER"
 * to ensure dining transactions are not classified as transport).
 *
 * Keywords are matched as whole words to avoid false positives where
 * a keyword appears as part of a larger word.
 *
 * @author Athenkosi Mvimbi
 */

@Service
public class CategorizationService {

    private record Rule(Category category, boolean creditOnly, List<Pattern> patterns) {
    }

    private final List<Rule> rules;

    public CategorizationService() {
        List<Rule> list = new ArrayList<>();
        list.add(rule(Category.SALARY, true, "SALARY", "PAYROLL", "WAGES"));
        list.add(rule(Category.RENT, false, "RENT", "LANDLORD", "BOND REPAYMENT"));
        list.add(rule(Category.FEES, false, "FEE", "FEES", "BANK CHARGES"));
        list.add(rule(Category.INSURANCE, false, "INSURANCE", "DISCOVERY", "OUTSURANCE", "SANLAM", "PREMIUM"));
        list.add(rule(Category.UTILITIES, false, "ESKOM", "ELECTRICITY", "MUNICIPALITY", "CITY POWER", "WATER"));
        list.add(rule(Category.GROCERIES, false, "CHECKERS", "PICK N PAY", "SHOPRITE", "SPAR", "WOOLWORTHS"));
        list.add(rule(Category.FUEL, false, "ENGEN", "SHELL", "BP", "SASOL", "FUEL"));
        list.add(rule(Category.AIRTIME_DATA, false, "VODACOM", "MTN", "TELKOM", "CELL C", "AIRTIME", "DATA"));
        list.add(rule(Category.DINING, false, "UBER EATS", "MR D", "NANDOS", "SPUR", "STEERS", "KFC", "RESTAURANT"));
        list.add(rule(Category.TRANSPORT, false, "UBER", "BOLT", "GAUTRAIN", "PRASA", "RIDE"));
        list.add(rule(Category.ENTERTAINMENT, false, "NETFLIX", "SHOWMAX", "DSTV", "SPOTIFY", "CINEMA", "NU METRO"));
        list.add(rule(Category.HEALTH, false, "DIS-CHEM", "CLICKS", "PHARMACY", "MEDICAL", "DOCTOR"));
        list.add(rule(Category.SHOPPING, false, "TAKEALOT", "MR PRICE", "TRUWORTHS", "PEP"));
        list.add(rule(Category.TRANSFERS, false, "TRANSFER", "TOP-UP", "TOPUP", "EFT"));
        this.rules = List.copyOf(list);
    }

    private static Rule rule(Category category, boolean creditOnly, String... keywords) {
        List<Pattern> patterns = new ArrayList<>();
        for (String keyword : keywords) {
            // (?<![A-Z0-9]) and (?![A-Z0-9]) act as word boundaries that also work around spaces/hyphens
            patterns.add(Pattern.compile("(?<![A-Z0-9])" + Pattern.quote(keyword) + "(?![A-Z0-9])"));
        }
        return new Rule(category, creditOnly, List.copyOf(patterns));
    }

    /** Picks a category for the transaction, falling back to OTHER when nothing matches. */
    public Category categorise(String merchant, String description, BigDecimal amount) {
        String text = (Objects.toString(merchant, "") + " " + Objects.toString(description, "")).toUpperCase(Locale.ROOT);
        boolean isCredit = amount != null && amount.signum() > 0;

        for (Rule rule : rules) {
            if (rule.creditOnly() && !isCredit) {
                continue; // e.g. a debit mentioning "salary" is not income
            }
            for (Pattern pattern : rule.patterns()) {
                if (pattern.matcher(text).find()) {
                    return rule.category();
                }
            }
        }
        return Category.OTHER;
    }
}
