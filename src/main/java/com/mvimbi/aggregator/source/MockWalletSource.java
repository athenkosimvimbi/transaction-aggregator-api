package com.mvimbi.aggregator.source;

import com.mvimbi.aggregator.domain.SourceSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.springframework.stereotype.Component;

/**
 * Mock mobile wallet: airtime and data, e-hailing rides, and wallet top-ups.
 *
 * @author Athenkosi Mvimbi
 */
@Component
public class MockWalletSource extends AbstractMockSource {

    @Override
    public SourceSystem system() {
        return SourceSystem.MOCK_WALLET;
    }

    @Override
    protected List<Draft> generateMonth(String customerRef, Random rnd) {
        List<Draft> out = new ArrayList<>();

        out.add(new Draft(2, "MOBILE WALLET", "EFT TOP-UP", rand(rnd, 500, 1500)));

        int airtime = 2 + rnd.nextInt(3);
        for (int i = 0; i < airtime; i++) {
            out.add(new Draft(day(rnd, 1, 28), pick(rnd, "VODACOM", "MTN", "TELKOM"), "AIRTIME TOPUP", debit(rand(rnd, 29, 299))));
        }

        int rides = 4 + rnd.nextInt(5);
        for (int i = 0; i < rides; i++) {
            out.add(new Draft(day(rnd, 1, 28), pick(rnd, "UBER", "BOLT"), "RIDE", debit(rand(rnd, 45, 220))));
        }

        if (rnd.nextBoolean()) {
            out.add(new Draft(day(rnd, 5, 25), "GAUTRAIN", "TRAIN FARE", debit(rand(rnd, 80, 190))));
        }
        return out;
    }
}
