package com.yamashiroya.payment_api.controller;

import com.yamashiroya.payment_api.entity.Product;
import com.yamashiroya.payment_api.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/recommended-products")
public class RecommendedProductController {

    private final ProductRepository productRepository;

    public RecommendedProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping
    public ResponseEntity<List<Product>> getRecommendedProducts() {
        List<Product> products = productRepository.findByRecommendedTrue();
        if (products == null) {
            products = Collections.emptyList();
        }
        return ResponseEntity.ok(products);
    }
}
