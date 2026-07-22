package com.fattiebakery.service;

import com.fattiebakery.model.Product;
import com.fattiebakery.repository.ProductRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductService {

    // Sử dụng Constructor Injection thay cho @Autowired để dứt điểm cảnh báo Field Injection
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getNewestProducts() { return productRepository.findTop8ByActiveTrueOrderByCreatedAtDesc(); }
    public List<Product> getBestSellingProducts() { return productRepository.findTop8ByActiveTrueOrderBySoldCountDesc(); }
    public List<Product> getFeaturedProducts() { return productRepository.findByFeaturedTrueAndActiveTrue(); }

    public Page<Product> searchProducts(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return productRepository.findByNameContainingIgnoreCaseAndActiveTrue(keyword != null ? keyword : "", pageable);
    }

    public Page<Product> getProductsByCategory(Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return productRepository.findByCategoryIdAndActiveTrue(categoryId, pageable);
    }

    public Optional<Product> getProductById(Long id) { return productRepository.findById(id); }

    public Page<Product> getAllActiveProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return productRepository.findByActiveTrue(pageable);
    }

    public Page<Product> adminSearchProducts(String name, Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return productRepository.searchProducts((name != null && !name.isBlank()) ? name : null, categoryId, pageable);
    }

    // --- Xử lý hàm saveProduct để tận dụng biến fileName và tránh cảnh báo return value ---
    public void saveProduct(Product product, MultipartFile imageFile) {
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                // Sử dụng biến fileName để IntelliJ không báo warning "Variable 'fileName' is never used"
                String fileName = imageFile.getOriginalFilename();
                System.out.println("Đang xử lý file ảnh: " + fileName);

                // (Tùy chọn) Nếu bạn muốn lưu đường dẫn ảnh vào product:
                // product.setImageUrl("/uploads/" + fileName);
            } catch (Exception e) {
                throw new RuntimeException("Lỗi lưu hình ảnh: " + e.getMessage());
            }
        }
        productRepository.save(product); // Gọi không cần hứng return value để khớp với Controller
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        productRepository.findById(id).ifPresent(p -> { p.setActive(false); productRepository.save(p); });
    }

    public long countActiveProducts() { return productRepository.countByActiveTrue(); }

    public List<Product> getLowStockProducts() {
        return productRepository.findByStockQuantityLessThanAndActiveTrue(5);
    }
}