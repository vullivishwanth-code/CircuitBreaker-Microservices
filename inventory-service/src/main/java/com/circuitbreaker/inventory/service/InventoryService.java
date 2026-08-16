package com.circuitbreaker.inventory.service;

import com.circuitbreaker.inventory.model.Inventory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryService {

    private final List<Inventory> inventoryList = List.of(
            new Inventory(1L, 12, "Dallas Warehouse"),
            new Inventory(2L, 28, "Austin Warehouse"),
            new Inventory(3L, 18, "Houston Warehouse"),
            new Inventory(4L, 42, "Dallas Warehouse"),
            new Inventory(5L, 15, "San Antonio Warehouse")
    );

    public List<Inventory> getAllInventory() {
        return inventoryList;
    }

    public Inventory getInventoryByProductId(Long productId) {
        return inventoryList.stream()
                .filter(inventory -> inventory.getProductId().equals(productId))
                .findFirst()
                .orElse(null);
    }
}