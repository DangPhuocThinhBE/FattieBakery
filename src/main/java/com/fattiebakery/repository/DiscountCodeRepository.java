package com.fattiebakery.repository;

import com.fattiebakery.model.DiscountCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiscountCodeRepository extends JpaRepository<DiscountCode, Long> {

    // 1. Tìm theo mã (không phân biệt hoa thường) - Dùng để kiểm tra sự tồn tại
    Optional<DiscountCode> findByCodeIgnoreCase(String code);

    // 2. Tìm theo mã + Phải đang hoạt động (Active = true)
    // Hàm này cực kỳ quan trọng để bác áp mã ở trang Giỏ hàng
    Optional<DiscountCode> findByCodeIgnoreCaseAndActiveTrue(String code);

    // 3. Lấy danh sách tất cả mã đang kích hoạt
    List<DiscountCode> findByActiveTrue();
}