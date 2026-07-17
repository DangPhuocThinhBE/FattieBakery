package com.fattiebakery.controller;

import com.fattiebakery.model.User;
import com.fattiebakery.service.CartService;
import com.fattiebakery.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired private CartService cartService;
    @Autowired private UserService userService;

    @ModelAttribute
    public void addAttributes(Model model, Authentication auth, HttpSession session, HttpServletRequest request) {
        String uri = request.getRequestURI();

        // 1. Thoát ngay nếu là trang tĩnh hoặc trang lỗi để tránh loop
        if (uri.contains("/error") || uri.contains("/login") || uri.contains("/images") || uri.contains("/uploads") || uri.contains("/css") || uri.contains("/js")) {
            return;
        }

        // 2. Kiểm tra đăng nhập
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal().toString())) {
            try {
                // SỬA Ở ĐÂY: Tìm bằng Email vì Principal lúc này đang chứa Email bác nhé!
                String loginId = auth.getName();
                User user = userService.findByEmail(loginId)
                        .orElseGet(() -> userService.findByUsername(loginId).orElse(null));

                if (user != null) {
                    model.addAttribute("user", user); // Bây giờ biến 'user' đã có avatarUrl chuẩn

                    try {
                        model.addAttribute("cartCount", cartService.getCartCount(user, session.getId()));
                    } catch (Exception e) {
                        model.addAttribute("cartCount", 0);
                    }
                }
            } catch (Exception e) {
                System.out.println("Lỗi nạp User Global: " + e.getMessage());
            }
        }
    }
}