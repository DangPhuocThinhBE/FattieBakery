package com.fattiebakery.controller;

import com.fattiebakery.service.DiscountCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/discount")
public class DiscountController {

    @Autowired
    private DiscountCodeService discountCodeService;

    @PostMapping("/apply")
    public ResponseEntity<?> applyDiscount(@RequestBody Map<String, Object> payload) {
        if (payload.get("code") == null || payload.get("orderAmount") == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Thiếu thông tin!"));
        }

        try {
            String code = (String) payload.get("code");
            BigDecimal orderAmount = new BigDecimal(payload.get("orderAmount").toString());
            BigDecimal discountAmount = discountCodeService.applyDiscount(code, orderAmount);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("discountAmount", discountAmount);
            response.put("finalAmount", orderAmount.subtract(discountAmount));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}