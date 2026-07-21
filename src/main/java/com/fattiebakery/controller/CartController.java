package com.fattiebakery.controller;

import com.fattiebakery.model.CartItem;
import com.fattiebakery.model.User;
import com.fattiebakery.service.CartService;
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

    private User getCurrentUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return null;
        return userService.findByUsername(auth.getName()).orElse(null);
    }

    @GetMapping("/cart")
    public String viewCart(Model model, Authentication auth, HttpSession session) {
        User user = getCurrentUser(auth);
        List<CartItem> cartItems = cartService.getCartItems(user, session.getId());
        BigDecimal total = cartService.getCartTotal(cartItems);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", total);
        return "user/cart";
    }

    @PostMapping("/api/cart/update-qty")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> apiUpdateQty(@RequestParam Long itemId, @RequestParam int quantity) {
        Map<String, Object> response = new HashMap<>();
        try {
            cartService.updateQuantity(itemId, quantity);
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/cart/remove")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> apiRemoveItem(@RequestParam Long itemId) {
        Map<String, Object> response = new HashMap<>();
        try {
            cartService.removeItem(itemId);
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cart/add")
    public String addCartStandard(@RequestParam Long productId,
                                  @RequestParam(defaultValue = "1") int quantity,
                                  Authentication auth, HttpSession session) {
        User user = getCurrentUser(auth);
        cartService.addToCart(productId, quantity, user, session.getId());
        return "redirect:/shop"; // Thêm xong ở lại trang shop, không bị nhảy qua trang giỏ hàng
    }

    @PostMapping("/api/cart/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> apiAddToCart(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") int quantity,
            Authentication auth, HttpSession session) {

        System.out.println("--> ĐÃ GỌI API THÊM GIỎ HÀNG: ProductID = " + productId + ", Quantity = " + quantity);

        Map<String, Object> response = new HashMap<>();
        try {
            User user = getCurrentUser(auth);
            cartService.addToCart(productId, quantity, user, session.getId());
            response.put("success", true);
            System.out.println("--> THÊM THÀNH CÔNG!");
        } catch (Exception e) {
            e.printStackTrace(); // In lỗi ra màn hình console để xem lỗi gì
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }
    // HÀM applyDiscount CŨ ĐÃ ĐƯỢC XÓA BỎ TẠI ĐÂY
}