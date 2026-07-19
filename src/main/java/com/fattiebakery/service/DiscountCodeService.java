package com.fattiebakery.service;

import com.fattiebakery.model.DiscountCode;
import com.fattiebakery.repository.DiscountCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DiscountCodeService {

    @Autowired
    private DiscountCodeRepository discountCodeRepository;

    // --- Các hàm quản lý cơ bản ---
    public List<DiscountCode> getAllDiscountCodes() {
        return discountCodeRepository.findAll();
    }

    public DiscountCode save(DiscountCode discountCode) {
        return discountCodeRepository.save(discountCode);
    }

    public void delete(Long id) {
        discountCodeRepository.deleteById(id);
    }

    // --- Hàm tìm kiếm và xử lý trạng thái ---
    public Optional<DiscountCode> findByCode(String code) {
        return discountCodeRepository.findByCodeIgnoreCase(code);
    }

    public void incrementUsage(DiscountCode discountCode) {
        discountCode.setCurrentUsageCount(discountCode.getCurrentUsageCount() + 1);
        discountCodeRepository.save(discountCode);
    }

    // --- Hàm tính toán giảm giá ---
    public BigDecimal applyDiscount(String code, BigDecimal orderAmount) {
        DiscountCode dc = discountCodeRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new RuntimeException("Mã không tồn tại!"));

        if (Boolean.FALSE.equals(dc.getActive()))
            throw new RuntimeException("Mã đã bị khóa.");

        LocalDateTime now = LocalDateTime.now();
        if (dc.getStartDate() != null && now.isBefore(dc.getStartDate()))
            throw new RuntimeException("Chưa đến thời gian áp dụng.");
        if (dc.getEndDate() != null && now.isAfter(dc.getEndDate()))
            throw new RuntimeException("Mã đã hết hạn.");
        if (dc.getMaxUsageCount() != null && dc.getCurrentUsageCount() >= dc.getMaxUsageCount())
            throw new RuntimeException("Mã đã hết lượt dùng.");
        if (dc.getMinimumOrderAmount() != null && orderAmount.compareTo(dc.getMinimumOrderAmount()) < 0)
            throw new RuntimeException("Đơn hàng tối thiểu: " + dc.getMinimumOrderAmount() + "đ");

        BigDecimal discountAmt = "PERCENTAGE".equalsIgnoreCase(dc.getDiscountType())
                ? orderAmount.multiply(dc.getDiscountValue()).divide(new BigDecimal("100"))
                : dc.getDiscountValue();

        return discountAmt.compareTo(orderAmount) > 0 ? orderAmount : discountAmt;
    }
}