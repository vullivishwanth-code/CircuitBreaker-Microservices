package com.circuitbreaker.product.client;

import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class RecommendationClient {

    private final RestClient restClient;
    private final CircuitBreaker circuitBreaker;

    public RecommendationClient(CircuitBreakerFactory<?, ?> circuitBreakerFactory) {
        this.restClient = RestClient.create("http://localhost:8083");
        this.circuitBreaker = circuitBreakerFactory.create("recommendationService");
    }

    public Map<String, Object> getRecommendations(Long productId) {
        return circuitBreaker.run(
                () -> restClient.get()
                        .uri("/api/recommendations/{productId}", productId)
                        .retrieve()
                        .body(Map.class),

                throwable -> Map.of(
                        "productId", productId,
                        "recommendations", List.of(
                                "Top Sellers",
                                "Trending Products"
                        ),
                        "fallback", true
                )
        );
    }
}