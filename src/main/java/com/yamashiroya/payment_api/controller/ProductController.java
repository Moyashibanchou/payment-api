package com.yamashiroya.payment_api.controller;

import com.yamashiroya.payment_api.entity.Product;
import com.yamashiroya.payment_api.entity.StringListConverter;
import com.yamashiroya.payment_api.repository.ProductRepository;
import com.yamashiroya.payment_api.service.CloudinaryService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductRepository productRepository;
    private final CloudinaryService cloudinaryService;

    public ProductController(ProductRepository productRepository, CloudinaryService cloudinaryService) {
        this.productRepository = productRepository;
        this.cloudinaryService = cloudinaryService;
    }

    // 全件取得 (GET)
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts(@RequestParam(value = "sort", required = false) String sort) {
        List<Product> products;
        if ("popular".equals(sort)) {
            products = productRepository.findAllByOrderByPurchaseCountDesc();
        } else {
            products = productRepository.findAll();
        }

        if (products == null) {
            products = Collections.emptyList();
        }
        return ResponseEntity.ok(products);
    }

    // IDで取得 (GET)
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 商品の追加 (POST)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Product createProduct(
            @RequestParam("image") MultipartFile image,
            @RequestParam("name") String name,
            @RequestParam("price") Integer price,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "style", required = false) String style,
            @RequestParam(value = "color", required = false) String color,
            @RequestParam(value = "purpose", required = false) List<String> purpose,
            @RequestParam(value = "recommended", required = false, defaultValue = "false") boolean recommended
    ) throws IOException {
        String imageUrl = cloudinaryService.upload(image);

        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setDescription(description);
        product.setImageUrl(imageUrl);
        product.setStyle(style);
        product.setColor(color);
        product.setPurpose(normalizePurposes(purpose));
        product.setRecommended(recommended);

        return productRepository.save(product);
    }

    // 商品の更新 (PUT)
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam("name") String name,
            @RequestParam("price") Integer price,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "style", required = false) String style,
            @RequestParam(value = "color", required = false) String color,
            @RequestParam(value = "purpose", required = false) List<String> purpose,
            @RequestParam(value = "recommended", required = false) Boolean recommended
    ) {
        return productRepository.findById(id)
                .map(product -> {
                    product.setName(name);
                    product.setPrice(price);
                    product.setDescription(description);
                    if (image != null && !image.isEmpty()) {
                        try {
                            product.setImageUrl(cloudinaryService.upload(image));
                        } catch (IOException e) {
                            throw new RuntimeException("画像のアップロードに失敗しました。", e);
                        }
                    }
                    product.setStyle(style);
                    product.setColor(color);
                    if (purpose != null) {
                        product.setPurpose(normalizePurposes(purpose));
                    }
                    if (recommended != null) {
                        product.setRecommended(recommended);
                    }
                    return ResponseEntity.ok(productRepository.save(product));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // 商品の削除 (DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(product -> {
                    productRepository.delete(product);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private List<String> normalizePurposes(List<String> purposes) {
        if (purposes == null || purposes.isEmpty()) {
            return Collections.emptyList();
        }

        if (purposes.size() == 1) {
            String only = purposes.get(0);
            if (only != null && only.contains(",")) {
                return StringListConverter.fromCsv(only);
            }
        }

        return purposes;
    }
}
