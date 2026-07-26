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

    // --- CÁC HÀM TÌM KIẾM CHO ADMIN/USER ---
    Optional<Order> findByOrderCode(String orderCode);

    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable);

    // Đã thêm ORDER BY o.id DESC để đơn mới nhất lên đầu và xử lý an toàn tham số status dạng String/Enum
    @Query("SELECT o FROM Order o WHERE " +
            "(:status IS NULL OR o.status = :status) AND " +
            "(:keyword IS NULL OR :keyword = '' OR LOWER(o.orderCode) LIKE LOWER(CONCAT('%',:keyword,'%')) " +
            "OR LOWER(o.customerName) LIKE LOWER(CONCAT('%',:keyword,'%')) " +
            "OR LOWER(o.customerPhone) LIKE LOWER(CONCAT('%',:keyword,'%'))) " +
            "ORDER BY o.id DESC")
    Page<Order> searchOrders(@Param("status") Order.OrderStatus status,
                             @Param("keyword") String keyword,
                             Pageable pageable);

    // --- CÁC HÀM THỐNG KÊ CHO DASHBOARD ---

    long countByStatus(Order.OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.finalAmount), 0) FROM Order o WHERE o.status = 'DELIVERED'")
    BigDecimal getTotalRevenue();

    @Query("SELECT MONTH(o.createdAt), SUM(o.finalAmount) " +
            "FROM Order o " +
            "WHERE YEAR(o.createdAt) = YEAR(CURRENT_DATE) AND o.status = 'DELIVERED' " +
            "GROUP BY MONTH(o.createdAt)")
    List<Object[]> getMonthlyRevenueRaw();

    Page<Order> findByOrderCodeContainingIgnoreCaseOrCustomerNameContainingIgnoreCase(String keyword, String keyword1, Pageable pageable);

    List<Order> findByUserId(Long userId);

    @Query("SELECT COALESCE(SUM(o.finalAmount), 0) FROM Order o WHERE o.createdAt BETWEEN :startDateTime AND :endDateTime AND o.status = 'DELIVERED'")
    BigDecimal getRevenueBetween(@Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime);

    @Query("SELECT FUNCTION('DATE', o.createdAt), SUM(o.finalAmount), COUNT(o) " +
            "FROM Order o " +
            "WHERE o.createdAt BETWEEN :startDateTime AND :endDateTime AND o.status = 'DELIVERED' " +
            "GROUP BY FUNCTION('DATE', o.createdAt) " +
            "ORDER BY FUNCTION('DATE', o.createdAt) ASC")
    List<Object[]> getDailyStatisticsRaw(@Param("startDateTime") LocalDateTime startDateTime,
                                         @Param("endDateTime") LocalDateTime endDateTime);
}