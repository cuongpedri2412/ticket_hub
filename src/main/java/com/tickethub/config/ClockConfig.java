package com.tickethub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Inject Clock thay vì gọi Instant.now() trực tiếp,
 * để trong test có thể "đóng băng" thời gian (Clock.fixed).
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
