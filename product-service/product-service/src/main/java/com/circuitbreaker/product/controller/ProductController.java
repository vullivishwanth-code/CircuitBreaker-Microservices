package com.circuitbreaker.product.controller;

import com.circuitbreaker.product.model.Product;
import com.circuitbreaker.product.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {

        Product product = productService.getProductById(id);

        if (product == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(product);
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<?> getProductDetails(@PathVariable Long id) {
        

        var details = productService.getProductWithInventory(id);

        if (details == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(details);
    }
}
