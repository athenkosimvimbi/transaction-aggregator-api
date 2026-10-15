package com.mvimbi.aggregator.source;

import com.mvimbi.aggregator.domain.SourceSystem;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.springframework.stereotype.Component;

/**
 * Mock credit card: retail, online shopping, subscriptions, eating out, pharmacy.
 *
 * @author Athenkosi Mvimbi
 */
@Component
public class MockCardSource extends AbstractMockSource {

    @Override
    public SourceSystem system() {
        return SourceSystem.MOCK_CARD;
    }

    @Override
    protected List<Draft> generateMonth(String customerRef, Random rnd) {
        List<Draft> out = new ArrayList<>();

        out.add(new Draft(12, "NETFLIX", "SUBSCRIPTION", new BigDecimal("-199.00")));
        out.add(new Draft(18, "SHOWMAX", "SUBSCRIPTION", new BigDecimal("-99.00")));

        int woolies = 1 + rnd.nextInt(3);
        for (int i = 0; i < woolies; i++) {
            out.add(new Draft(day(rnd, 2, 28), "WOOLWORTHS", "CARD PURCHASE WOOLWORTHS", debit(rand(rnd, 120, 850))));
        }

        int online = 1 + rnd.nextInt(2);
        for (int i = 0; i < online; i++) {
            out.add(new Draft(day(rnd, 2, 28), pick(rnd, "TAKEALOT", "MR PRICE", "TRUWORTHS"), "ONLINE PURCHASE", debit(rand(rnd, 200, 2500))));
        }

        int meals = 3 + rnd.nextInt(3);
        for (int i = 0; i < meals; i++) {
            out.add(new Draft(day(rnd, 1, 28), pick(rnd, "NANDOS", "SPUR", "STEERS", "UBER EATS", "MR D FOOD"),
                    "CARD PURCHASE", debit(rand(rnd, 80, 450))));
        }

        int pharmacy = 1 + rnd.nextInt(2);
        for (int i = 0; i < pharmacy; i++) {
            out.add(new Draft(day(rnd, 3, 27), pick(rnd, "DIS-CHEM", "CLICKS"), "PHARMACY PURCHASE", debit(rand(rnd, 60, 600))));
        }
        return out;
    }
}
