package com.fattiebakery.controller;

import com.fattiebakery.model.DiscountCode;
import com.fattiebakery.repository.DiscountCodeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/discount")
public class DiscountApiController {

    private final DiscountCodeRepository discountCodeRepository;

    public DiscountApiController(DiscountCodeRepository discountCodeRepository) {
        this.discountCodeRepository = discountCodeRepository;
    }

    @PostMapping("/apply")
    public ResponseEntity<Map<String, Object>> applyDiscount(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        String code = (String) request.get("code");
        Object amountObj = request.get("orderAmount");
        BigDecimal orderAmount = BigDecimal.ZERO;
        if (amountObj instanceof Number) {
            orderAmount = BigDecimal.valueOf(((Number) amountObj).doubleValue());
        }

        if (code == null || code.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Vui lòng nhập mã giảm giá!");
            return ResponseEntity.ok(response);
        }

        Optional<DiscountCode> discountOpt = discountCodeRepository.findByCodeIgnoreCaseAndActiveTrue(code.trim());

        if (discountOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Mã giảm giá không tồn tại hoặc đã bị khóa!");
            return ResponseEntity.ok(response);
        }

        DiscountCode discount = discountOpt.get();
        LocalDateTime now = LocalDateTime.now();

        // 1. Kiểm tra thời gian hiệu lực (startDate và endDate)
        if (discount.getStartDate() != null && now.isBefore(discount.getStartDate())) {
            response.put("success", false);
            response.put("message", "Mã giảm giá chưa đến thời gian sử dụng!");
            return ResponseEntity.ok(response);
        }

        if (discount.getEndDate() != null && now.isAfter(discount.getEndDate())) {
            response.put("success", false);
            response.put("message", "Mã giảm giá đã hết hạn sử dụng!");
            return ResponseEntity.ok(response);
        }

        // 2. Kiểm tra giá trị đơn hàng tối thiểu (minimumOrderAmount)
        if (discount.getMinimumOrderAmount() != null && orderAmount.compareTo(discount.getMinimumOrderAmount()) < 0) {
            response.put("success", false);
            response.put("message", "Đơn hàng chưa đạt giá trị tối thiểu (" + discount.getMinimumOrderAmount() + "đ) để dùng mã này!");
            return ResponseEntity.ok(response);
        }
        // 3. Kiểm tra số lần sử dụng tối đa (maxUsageCount)
        if (discount.getMaxUsageCount() != null && discount.getCurrentUsageCount() != null
        && discount.getCurrentUsageCount() >= discount.getMaxUsageCount()) {
        response.put("success", false);
            response.put("message", "Mã giảm giá đã hết lượt sử dụng!");
            return ResponseEntity.ok(response);
        }

        // 4. Tính toán số tiền được giảm dựa theo discountType ("PERCENT" hoặc "FIXED" / tùy bạn định nghĩa)
        BigDecimal discountAmount = BigDecimal.ZERO;
        String type = discount.getDiscountType() != null ? discount.getDiscountType().toUpperCase() : "";

            if (type.contains("PERCENT") || type.contains("%")) {
        // Giảm theo %
                discountAmount = orderAmount.multiply(discount.getDiscountValue())
                    .divide(BigDecimal.valueOf(100));
        } else {
        // Giảm số tiền cố định
                discountAmount = discount.getDiscountValue();
        }

                // Đảm bảo tiền giảm không vượt quá tổng đơn hàng
            if (discountAmount.compareTo(orderAmount) > 0) {
                discountAmount = orderAmount;
        }

        BigDecimal finalAmount = orderAmount.subtract(discountAmount);

        response.put("success", true);
        response.put("message", "Áp dụng mã giảm giá thành công!");
        response.put("discountAmount", discountAmount);
        response.put("finalAmount", finalAmount);

        return ResponseEntity.ok(response);
    }
            }