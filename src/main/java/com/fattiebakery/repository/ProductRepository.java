package com.fattiebakery.repository;

import com.fattiebakery.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Tìm kiếm theo tên (User)
    Page<Product> findByNameContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);

    // Theo danh mục (User)
    Page<Product> findByCategoryIdAndActiveTrue(Long categoryId, Pageable pageable);

    // Mới nhất (User)
    List<Product> findTop8ByActiveTrueOrderByCreatedAtDesc();

    // Bán chạy nhất (User)
    List<Product> findTop8ByActiveTrueOrderBySoldCountDesc();

    // Nổi bật (User)
    List<Product> findByFeaturedTrueAndActiveTrue();

    // Admin: tìm kiếm + phân trang (Tối ưu hóa điều kiện null)
    @Query("SELECT p FROM Product p WHERE " +
            "(:name IS NULL OR :name = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:categoryId IS NULL OR p.category.id = :categoryId)")
    Page<Product> searchProducts(@Param("name") String name,
                                 @Param("categoryId") Long categoryId,
                                 Pageable pageable);

    // Thống kê: tổng sản phẩm active
    long countByActiveTrue();

    // Sản phẩm sắp hết hàng (Dùng ngưỡng threshold)
    List<Product> findByStockQuantityLessThanAndActiveTrue(int threshold);

    // Lấy tất cả sản phẩm đang active (User)
    Page<Product> findByActiveTrue(Pageable pageable);


}