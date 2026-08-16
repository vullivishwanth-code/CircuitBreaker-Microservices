package com.circuitbreaker.inventory.model;

public class Inventory {

    private Long productId;
    private int availableQuantity;
    private String warehouseLocation;

    public Inventory() {
    }

    public Inventory(Long productId, int availableQuantity, String warehouseLocation) {
        this.productId = productId;
        this.availableQuantity = availableQuantity;
        this.warehouseLocation = warehouseLocation;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public String getWarehouseLocation() {
        return warehouseLocation;
    }

    public void setWarehouseLocation(String warehouseLocation) {
        this.warehouseLocation = warehouseLocation;
    }
}
