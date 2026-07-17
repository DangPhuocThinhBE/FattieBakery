package com.fattiebakery.repository;

import com.fattiebakery.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderCode(String orderCode);

    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT o FROM Order o WHERE " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:keyword IS NULL OR LOWER(o.orderCode) LIKE LOWER(CONCAT('%',:keyword,'%')) " +
           "OR LOWER(o.customerName) LIKE LOWER(CONCAT('%',:keyword,'%')) " +
           "OR LOWER(o.customerPhone) LIKE LOWER(CONCAT('%',:keyword,'%')))")
    Page<Order> searchOrders(@Param("status") Order.OrderStatus status,
                              @Param("keyword") String keyword,
                              Pageable pageable);

    // Thống kê doanh thu
    @Query("SELECT COALESCE(SUM(o.finalAmount), 0) FROM Order o WHERE o.status = 'DELIVERED'")
    BigDecimal getTotalRevenue();

    @Query("SELECT COALESCE(SUM(o.finalAmount), 0) FROM Order o WHERE o.status = 'DELIVERED' AND o.createdAt >= :startDate")
    BigDecimal getRevenueFrom(@Param("startDate") LocalDateTime startDate);

    long countByStatus(Order.OrderStatus status);

    // Doanh thu theo tháng (12 tháng gần nhất)
    @Query(value = "SELECT MONTH(created_at) as month, YEAR(created_at) as year, SUM(final_amount) as revenue " +
                   "FROM orders WHERE status = 'DELIVERED' AND created_at >= DATE_SUB(NOW(), INTERVAL 12 MONTH) " +
                   "GROUP BY YEAR(created_at), MONTH(created_at) ORDER BY year, month", nativeQuery = true)
    List<Object[]> getMonthlyRevenue();

    Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable);

    @Query("SELECT MONTH(o.createdAt), SUM(o.finalAmount) " +
            "FROM Order o " +
            "WHERE YEAR(o.createdAt) = YEAR(CURRENT_DATE) AND o.status = 'DELIVERED' " +
            "GROUP BY MONTH(o.createdAt)")
    List<Object[]> getMonthlyRevenueRaw();
}
