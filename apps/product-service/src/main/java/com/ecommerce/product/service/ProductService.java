package com.ecommerce.product.service;

import com.ecommerce.product.model.Product;
import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.data.redis.core.RedisTemplate;
import com.ecommerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.ecommerce.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final Cache<String, Product> caffeineCache;
    private final RedisTemplate<String, Product> redisTemplate;

    public Product getProduct(String id) {
    log.info("Fetching product with id: {}", id);

    String key = "product:" + id;

    //  L1 Cache (Caffeine)
    Product product = caffeineCache.getIfPresent(key);
    if (product != null) {
        log.info("Cache hit (Caffeine)");
        return product;
    }

    //  L2 Cache (Redis)
    product = redisTemplate.opsForValue().get(key);
    if (product != null) {
        log.info("Cache hit (Redis)");

        // promote to L1
        caffeineCache.put(key, product);
        return product;
    }

    //  DB
    product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

    // 4. Populate caches
    caffeineCache.put(key, product);
    redisTemplate.opsForValue().set(key, product, 10, TimeUnit.MINUTES);

    return product;
}

   
    public List<Product> getAllProducts() {
        log.info("Fetching all products");
        return productRepository.findAll();
    }

   
    public Product createProduct(Product product) {
    LocalDateTime now = LocalDateTime.now();
    product.setCreatedAt(now);
    product.setUpdatedAt(now);

    Product saved = productRepository.save(product);

    String key = "product:" + saved.getId();

    caffeineCache.put(key, saved);
    redisTemplate.opsForValue().set(key, saved, 10, TimeUnit.MINUTES);

    return saved;
}

   
    public Product updateProduct(String id, Product productDetails) {
    Product existing = productRepository.findById(id)
    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

    existing.setName(productDetails.getName());
    existing.setDescription(productDetails.getDescription());
    existing.setPrice(productDetails.getPrice());
    existing.setCategory(productDetails.getCategory());
    existing.setStockQuantity(productDetails.getStockQuantity());
    existing.setImageUrl(productDetails.getImageUrl());
    existing.setUpdatedAt(LocalDateTime.now());

    Product updated;
try {
    updated = productRepository.save(existing);
} catch (org.springframework.dao.OptimisticLockingFailureException e) {
    throw new RuntimeException("Product was updated by another request. Retry.");
}

    String key = "product:" + id;

    caffeineCache.put(key, updated);
    redisTemplate.opsForValue().set(key, updated, 10, TimeUnit.MINUTES);

    return updated;
}

   
    public void deleteProduct(String id) {
    Product existing = productRepository.findById(id)
    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        productRepository.delete(existing);

            String key = "product:" + id;

            caffeineCache.invalidate(key);
            redisTemplate.delete(key);
        }

    public List<Product> getProductsByCategory(String category) {
        log.info("Fetching products by category: {}", category);
        return productRepository.findByCategory(category);
    }

    public List<Product> searchProducts(String name) {
        log.info("Searching products by name: {}", name);
        return productRepository.findByNameContainingIgnoreCase(name);
    }
}