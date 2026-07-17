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

    public DiscountCode save(DiscountCode discountCode) {
        return discountCodeRepository.save(discountCode);
    }

    @Autowired
    private DiscountCodeRepository discountCodeRepository;

    public List<DiscountCode> getAllDiscountCodes() {
        return discountCodeRepository.findAll();
    }

    public List<DiscountCode> getActiveDiscountCodes() {
        return discountCodeRepository.findByActiveTrue();
    }

    public Optional<DiscountCode> findByCode(String code) {
        return discountCodeRepository.findByCodeIgnoreCase(code);
    }

    public void delete(Long id) {
        discountCodeRepository.deleteById(id);
    }

    // --- HÀM ÁP DỤNG MÃ GIẢM GIÁ (BÁC THAY ĐOẠN NÀY NHÉ) ---
    public BigDecimal applyDiscount(String code, BigDecimal orderAmount) {
        DiscountCode dc = discountCodeRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new RuntimeException("Mã giảm giá '" + code + "' không tồn tại!"));

        // 1. Kiểm tra trạng thái Kích hoạt
        if (dc.getActive() == null || !dc.getActive()) {
            throw new RuntimeException("Mã giảm giá này hiện đã bị tạm khóa.");
        }

        // 2. Kiểm tra Thời hạn (Dựa trên thời gian thực tế 2026)
        LocalDateTime now = LocalDateTime.now();
        if (dc.getStartDate() != null && now.isBefore(dc.getStartDate())) {
            throw new RuntimeException("Mã giảm giá này chưa đến thời gian áp dụng.");
        }
        if (dc.getEndDate() != null && now.isAfter(dc.getEndDate())) {
            throw new RuntimeException("Mã giảm giá đã hết hạn sử dụng mất rồi!");
        }

        // 3. Kiểm tra Lượt dùng
        if (dc.getMaxUsageCount() != null && dc.getCurrentUsageCount() >= dc.getMaxUsageCount()) {
            throw new RuntimeException("Mã giảm giá đã hết lượt sử dụng!");
        }

        // 4. Kiểm tra Giá trị đơn hàng tối thiểu (Minimum Order Amount)
        if (dc.getMinimumOrderAmount() != null && orderAmount.compareTo(dc.getMinimumOrderAmount()) < 0) {
            throw new RuntimeException("Đơn hàng của bác phải từ " + dc.getMinimumOrderAmount().longValue() + "đ mới áp được mã này.");
        }

        // 5. Tính toán số tiền giảm dựa trên DiscountType
        BigDecimal discountAmt = BigDecimal.ZERO;
        if ("PERCENTAGE".equalsIgnoreCase(dc.getDiscountType())) {
            // Tính % (Ví dụ: 10% của 200k = 20k)
            discountAmt = orderAmount.multiply(dc.getDiscountValue()).divide(new BigDecimal(100));
        } else if ("FIXED_AMOUNT".equalsIgnoreCase(dc.getDiscountType())) {
            // Giảm thẳng số tiền (Ví dụ: 50.000đ)
            discountAmt = dc.getDiscountValue();
        }

        // Đảm bảo số tiền giảm không vượt quá tổng giá trị đơn hàng
        if (discountAmt.compareTo(orderAmount) > 0) {
            discountAmt = orderAmount;
        }

        return discountAmt;
    }

    public void incrementUsage(DiscountCode dc) {
        dc.setCurrentUsageCount(dc.getCurrentUsageCount() + 1);
        discountCodeRepository.save(dc);
    }
}