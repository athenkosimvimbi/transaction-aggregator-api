package com.mvimbi.aggregator.source;

import com.mvimbi.aggregator.domain.SourceSystem;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.springframework.stereotype.Component;

/**
 * Mock current/cheque account: salary in, rent, utilities, groceries, fuel and so on.
 *
 * @author Athenkosi Mvimbi
 */
@Component
public class MockBankSource extends AbstractMockSource {

    @Override
    public SourceSystem system() {
        return SourceSystem.MOCK_BANK;
    }

    @Override
    protected List<Draft> generateMonth(String customerRef, Random rnd) {
        List<Draft> out = new ArrayList<>();
        int salary = baseSalary(customerRef);

        out.add(new Draft(1, "LANDLORD EFT", "RENT PAYMENT", debit(BigDecimal.valueOf(salary * 30L / 100, 0).setScale(2))));
        out.add(new Draft(25, "EMPLOYER PAYROLL", "SALARY PAYMENT", BigDecimal.valueOf(salary, 0).setScale(2)));
        out.add(new Draft(26, "SAVINGS ACCOUNT", "EFT TRANSFER TO SAVINGS", debit(rand(rnd, 500, 2000))));
        out.add(new Draft(28, "BANK", "MONTHLY ACCOUNT FEE", new BigDecimal("-69.50")));
        out.add(new Draft(15, "DISCOVERY INSURE", "INSURANCE PREMIUM", debit(rand(rnd, 650, 1200))));
        out.add(new Draft(day(rnd, 5, 12), "ESKOM", "PREPAID ELECTRICITY", debit(rand(rnd, 300, 800))));

        int groceryTrips = 3 + rnd.nextInt(3);
        for (int i = 0; i < groceryTrips; i++) {
            String shop = pick(rnd, "CHECKERS", "PICK N PAY", "SHOPRITE", "SPAR");
            out.add(new Draft(day(rnd, 2, 28), shop, "POS PURCHASE " + shop, debit(rand(rnd, 150, 900))));
        }

        int fuelStops = 2 + rnd.nextInt(2);
        for (int i = 0; i < fuelStops; i++) {
            out.add(new Draft(day(rnd, 3, 27), pick(rnd, "ENGEN", "SHELL", "BP"), "FUEL PURCHASE", debit(rand(rnd, 400, 900))));
        }
        return out;
    }
}
