package com.circuitbreaker.recommendation.controller;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    @GetMapping("/{productId}")
    public Map<String, Object> getRecommendations(@PathVariable Long productId) {
        return Map.of(
                "productId", productId,
                "recommendations", List.of(
                        "Top Sellers",
                        "Trending Products",
                        "Customers Also Viewed"
                )
        );
    }
}