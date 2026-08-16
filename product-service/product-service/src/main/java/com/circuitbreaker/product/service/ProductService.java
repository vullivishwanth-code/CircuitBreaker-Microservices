package com.circuitbreaker.product.service;

import com.circuitbreaker.product.client.InventoryClient;
import com.circuitbreaker.product.client.RecommendationClient;
import com.circuitbreaker.product.model.Product;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductService {

    private final InventoryClient inventoryClient;
    private final RecommendationClient recommendationClient;

    public ProductService(
            InventoryClient inventoryClient,
            RecommendationClient recommendationClient
    ) {
        this.inventoryClient = inventoryClient;
        this.recommendationClient = recommendationClient;
    }

    private final List<Product> products = List.of(
            new Product(
                    1L,
                    "Apple MacBook Pro 14",
                    "Laptops",
                    1999.99,
                    12,
                    "High-performance laptop with Apple silicon for professional workloads."
            ),
            new Product(
                    2L,
                    "Sony WH-1000XM5",
                    "Audio",
                    349.99,
                    28,
                    "Premium wireless headphones with active noise cancellation."
            ),
            new Product(
                    3L,
                    "Samsung Galaxy S25 Ultra",
                    "Smartphones",
                    1299.99,
                    18,
                    "Flagship Android smartphone with advanced camera and performance features."
            ),
            new Product(
                    4L,
                    "Logitech MX Master 3S",
                    "Accessories",
                    99.99,
                    42,
                    "Ergonomic wireless productivity mouse designed for professional workflows."
            ),
            new Product(
                    5L,
                    "Dell UltraSharp 27 Monitor",
                    "Monitors",
                    549.99,
                    15,
                    "27-inch professional monitor with high-resolution display and USB-C connectivity."
            )
    );

    public List<Product> getAllProducts() {
        return products;
    }

    public Product getProductById(Long id) {
        return products.stream()
                .filter(product -> product.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public Map<String, Object> getProductWithInventory(Long id) {

        Product product = getProductById(id);

        if (product == null) {
            return null;
        }

        Map<String, Object> inventory =
                inventoryClient.getInventory(id);

        Map<String, Object> recommendations =
                recommendationClient.getRecommendations(id);

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("product", product);
        response.put("inventory", inventory);
        response.put("recommendations", recommendations);

        return response;
    }
}