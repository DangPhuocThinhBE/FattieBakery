package com.fattiebakery.service;

import com.fattiebakery.model.Product;
import com.fattiebakery.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    // ====== USER FUNCTIONS ======

    public List<Product> getNewestProducts() {
        return productRepository.findTop8ByActiveTrueOrderByCreatedAtDesc();
    }

    public List<Product> getBestSellingProducts() {
        return productRepository.findTop8ByActiveTrueOrderBySoldCountDesc();
    }

    public List<Product> getFeaturedProducts() {
        return productRepository.findByFeaturedTrueAndActiveTrue();
    }

    public Page<Product> searchProducts(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return productRepository.findByNameContainingIgnoreCaseAndActiveTrue(keyword, pageable);
    }

    public Page<Product> getProductsByCategory(Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return productRepository.findByCategoryIdAndActiveTrue(categoryId, pageable);
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Page<Product> getAllActiveProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return productRepository.findAll(pageable);
    }

    // ====== ADMIN FUNCTIONS ======

    public Page<Product> adminSearchProducts(String name, Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return productRepository.searchProducts(
                (name != null && !name.isBlank()) ? name : null,
                categoryId,
                pageable);
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        productRepository.findById(id).ifPresent(p -> {
            p.setActive(false);
            productRepository.save(p);
        });
    }

    public void hardDeleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    public long countActiveProducts() {
        return productRepository.countByActiveTrue();
    }

    public List<Product> getLowStockProducts() {
        return productRepository.findByStockQuantityLessThanAndActiveTrue(5);
    }
}
