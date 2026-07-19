package com.fattiebakery.repository;

import com.fattiebakery.model.DiscountCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiscountCodeRepository extends JpaRepository<DiscountCode, Long> {
    Optional<DiscountCode> findByCodeIgnoreCase(String code);
    Optional<DiscountCode> findByCodeIgnoreCaseAndActiveTrue(String code);
    List<DiscountCode> findByActiveTrue();
    // Đã xóa bỏ hoàn toàn LazyConstant và Optional<Object>
}