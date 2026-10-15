package com.mvimbi.aggregator.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides a Clock bean for application-wide date and time operations.
 * Using a configurable Clock improves testability and ensures time is
 * consistently resolved using the Africa/Johannesburg time zone.
 *
 * @author Athenkosi Mvimbi
 */

@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("Africa/Johannesburg"));
    }
}
