package com.circuitbreaker.gateway;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class BulkheadConfiguration {

    @Bean
    public Bulkhead productBulkhead() {

        BulkheadConfig config = BulkheadConfig.custom()
                .maxConcurrentCalls(3)
                .maxWaitDuration(Duration.ZERO)
                .build();

        BulkheadRegistry registry = BulkheadRegistry.of(config);

        return registry.bulkhead("productBulkhead");
    }
}