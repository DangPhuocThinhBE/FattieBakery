package com.fattiebakery.controller;

import com.fattiebakery.model.CartItem;
import com.fattiebakery.model.User;
import com.fattiebakery.service.CartService;
import com.fattiebakery.service.DiscountCodeService;
import com.fattiebakery.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@Controller
public class CartController {

    @Autowired private CartService cartService;
    @Autowired private UserService userService;
    @Autowired private DiscountCodeService discountCodeService;

    private User getCurrentUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return null;
        return userService.findByUsername(auth.getName()).orElse(null);
    }

    // Trang giỏ hàng chính
    @GetMapping("/cart")
    public String viewCart(Model model, Authentication auth, HttpSession session) {
        User user = getCurrentUser(auth);
        List<CartItem> cartItems = cartService.getCartItems(user, session.getId());
        BigDecimal total = cartService.getCartTotal(cartItems);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", total);
        return "user/cart";
    }

    // --- API CẬP NHẬT SỐ LƯỢNG (Dành cho AJAX trong HTML của bác) ---
    @PostMapping("/api/cart/update-qty")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> apiUpdateQty(
            @RequestParam Long itemId,
            @RequestParam int quantity) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (quantity < 1) throw new Exception("Số lượng không hợp lệ");

            cartService.updateQuantity(itemId, quantity);

            response.put("success", true);
            response.put("message", "Cập nhật thành công");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    // --- API XÓA SẢN PHẨM (Dành cho AJAX trong HTML của bác) ---
    @PostMapping("/api/cart/remove")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> apiRemoveItem(@RequestParam Long itemId) {
        Map<String, Object> response = new HashMap<>();
        try {
            cartService.removeItem(itemId);
            response.put("success", true);
            response.put("message", "Đã xóa sản phẩm");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    // API: Thêm vào giỏ (Giữ nguyên)
    @PostMapping("/api/cart/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> apiAddToCart(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") int quantity,
            Authentication auth, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = getCurrentUser(auth);
            cartService.addToCart(productId, quantity, user, session.getId());
            long count = cartService.getCartCount(user, session.getId());
            response.put("success", true);
            response.put("cartCount", count);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    // API: Áp dụng mã giảm giá (Giữ nguyên)
    @PostMapping("/api/discount/apply")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> applyDiscount(
            @RequestParam String code,
            @RequestParam BigDecimal orderAmount) {
        Map<String, Object> response = new HashMap<>();
        try {
            BigDecimal discount = discountCodeService.applyDiscount(code, orderAmount);
            response.put("success", true);
            response.put("discountAmount", discount);
            response.put("finalAmount", orderAmount.subtract(discount));
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }
}