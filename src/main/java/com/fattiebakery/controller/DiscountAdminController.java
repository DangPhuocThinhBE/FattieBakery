package com.fattiebakery.controller;

import com.fattiebakery.model.DiscountCode;
import com.fattiebakery.service.DiscountCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class DiscountAdminController {

    @Autowired
    private DiscountCodeService discountCodeService;

    // 1. Phục vụ hiển thị giao diện quản lý mã giảm giá của Admin
    @GetMapping("/admin/discounts")
    public String viewDiscountsPage(Model model) {
        model.addAttribute("discounts", discountCodeService.getAllDiscountCodes());
        model.addAttribute("discount", new DiscountCode());
        return "admin/discounts";
    }

    // 2. Phục vụ lưu/cập nhật dữ liệu từ form admin
    @PostMapping("/admin/discounts/save")
    public String saveDiscount(@ModelAttribute("discount") DiscountCode discountCode) {
        discountCodeService.save(discountCode);
        return "redirect:/admin/discounts";
    }

    // 3. API xử lý áp dụng mã giảm giá chuẩn xác cho trang giỏ hàng (tính đúng % và số tiền)
    @ResponseBody
    @PostMapping("/api/discount/apply")
    public ResponseEntity<Map<String, Object>> applyDiscount(@RequestBody Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();

        String code = (String) payload.get("code");
        Object orderAmountObj = payload.get("orderAmount");

        BigDecimal orderTotal = BigDecimal.ZERO;
        if (orderAmountObj != null) {
            try {
                orderTotal = new BigDecimal(orderAmountObj.toString());
            } catch (Exception e) {
                orderTotal = BigDecimal.ZERO;
            }
        }

        if (code == null || code.isBlank()) {
            response.put("success", false);
            response.put("message", "Vui lòng nhập mã giảm giá!");
            return ResponseEntity.ok(response);
        }

        Optional<DiscountCode> discountOpt = discountCodeService.getAllDiscountCodes().stream()
                .filter(d -> d.getCode() != null && d.getCode().equalsIgnoreCase(code.trim()))
                .findFirst();

        if (discountOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Mã giảm giá không tồn tại!");
            return ResponseEntity.ok(response);
        }

        DiscountCode discount = discountOpt.get();
        LocalDateTime now = LocalDateTime.now();

        if (Boolean.FALSE.equals(discount.getActive())) {
            response.put("success", false);
            response.put("message", "Mã giảm giá này hiện không hoạt động!");
            return ResponseEntity.ok(response);
        }

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

        if (discount.getMinimumOrderAmount() != null && orderTotal.compareTo(discount.getMinimumOrderAmount()) < 0) {
            response.put("success", false);
            response.put("message", "Đơn hàng chưa đạt giá trị tối thiểu (" + discount.getMinimumOrderAmount() + "đ) để dùng mã này!");
            return ResponseEntity.ok(response);
        }

        // --- LOGIC TÍNH TOÁN TIỀN GIẢM THEO PHẦN TRĂM HOẶC CỐ ĐỊNH ---
        BigDecimal discountAmount = BigDecimal.ZERO;
        String type = discount.getDiscountType() != null ? discount.getDiscountType().trim() : "";
        BigDecimal val = discount.getDiscountValue() != null ? discount.getDiscountValue() : BigDecimal.ZERO;

        // Nếu type là PERCENT (10%, 20%, 30%...)
        if ("PERCENT".equalsIgnoreCase(type) || "%".equals(type)) {
            // Công thức: orderTotal * val / 100 (Làm tròn 0 chữ số thập phân để ra số tiền nguyên)
            discountAmount = orderTotal.multiply(val).divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP);
        } else {
            // Giảm theo số tiền cố định
            discountAmount = val;
        }

        // Đảm bảo tiền giảm không vượt quá tổng tiền đơn hàng
        if (discountAmount.compareTo(orderTotal) > 0) {
            discountAmount = orderTotal;
        }

        BigDecimal finalAmount = orderTotal.subtract(discountAmount);

        // Trả kết quả về cho Javascript ở giao diện giỏ hàng
        response.put("success", true);
        response.put("message", "Áp dụng mã giảm giá thành công!");
        response.put("discountAmount", discountAmount);
        response.put("finalAmount", finalAmount);

        return ResponseEntity.ok(response);
    }
}