package com.circuitbreaker.product.client;

import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class InventoryClient {

    private final RestClient restClient;
    private final CircuitBreaker circuitBreaker;

    public InventoryClient(CircuitBreakerFactory<?, ?> circuitBreakerFactory) {

        this.restClient = RestClient.create("http://localhost:8082");

        this.circuitBreaker =
                circuitBreakerFactory.create("inventoryService");
    }

    public Map<String, Object> getInventory(Long productId) {

        return circuitBreaker.run(
                () -> restClient.get()
                        .uri("/api/inventory/{productId}", productId)
                        .retrieve()
                        .body(Map.class),

                throwable -> inventoryFallback(productId, throwable)
        );
    }

    private Map<String, Object> inventoryFallback(
            Long productId,
            Throwable throwable) {

        Map<String, Object> fallback = new LinkedHashMap<>();

        fallback.put("productId", productId);
        fallback.put("availableQuantity", 0);
        fallback.put(
                "warehouseLocation",
                "Inventory service temporarily unavailable"
        );
        fallback.put("fallback", true);

        return fallback;
    }
}