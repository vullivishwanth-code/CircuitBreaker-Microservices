package com.circuitbreaker.gateway;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.caffeine.CaffeineProxyManager;
import io.github.bucket4j.distributed.proxy.AsyncProxyManager;
import io.github.bucket4j.distributed.remote.RemoteBucketState;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimiterConfiguration {

    @Bean
    public AsyncProxyManager<String> caffeineProxyManager() {

        Caffeine<Object, Object> caffeine =
                Caffeine.newBuilder()
                        .maximumSize(100);

        CaffeineProxyManager<String> proxyManager =
                new CaffeineProxyManager<>(
                        caffeine,
                        Duration.ofMinutes(1)
                );

        return proxyManager.asAsync();
    }
}