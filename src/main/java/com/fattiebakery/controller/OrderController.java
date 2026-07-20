package com.fattiebakery.controller;

import com.fattiebakery.model.Order;
import com.fattiebakery.model.User;
import com.fattiebakery.service.OrderService;
import com.fattiebakery.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/orders")
public class OrderController {

    @Autowired private OrderService orderService;
    @Autowired private UserService userService;

    // Helper: Lấy thông tin user hiện tại
    private User getCurrentUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return null;
        return userService.findByUsername(auth.getName()).orElse(null);
    }

    // 1. Danh sách đơn hàng của tôi
    @GetMapping
    public String myOrders(Model model, Authentication auth) {
        User user = getCurrentUser(auth);
        if (user == null) {
            return "redirect:/login";
        }

        // Lấy danh sách từ Service
        List<Order> orders = orderService.getUserOrders(user.getId());

        // Truyền vào model để hiển thị trong HTML
        model.addAttribute("orders", orders);
        return "user/my-orders";
    }

    @GetMapping("/orders")
    public String orders(@RequestParam(defaultValue = "0") int page, Model model) {
        // Không ép kiểu thủ công nếu không cần thiết, hoặc khai báo Page cụ thể
        Page<Order> orderPage = orderService.adminSearchOrders(null, null, page, 10);
        model.addAttribute("orders", orderPage);
        return "admin/orders";
    }

    // 2. Xem chi tiết đơn hàng
    @GetMapping("/{orderCode}")
    public String orderDetail(@PathVariable String orderCode,
                              Model model,
                              Authentication auth,
                              RedirectAttributes ra) {
        User user = getCurrentUser(auth);
        if (user == null) {
            return "redirect:/login";
        }

        Optional<Order> orderOpt = orderService.findByOrderCode(orderCode);

        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();

            // Kiểm tra bảo mật: Chỉ cho phép chủ nhân đơn hàng xem
            if (!order.getUser().getId().equals(user.getId())) {
                ra.addFlashAttribute("error", "Bạn không có quyền xem đơn hàng này!");
                return "redirect:/orders";
            }

            model.addAttribute("order", order);
            return "user/order-detail";
        }

        ra.addFlashAttribute("error", "Không tìm thấy đơn hàng!");
        return "redirect:/orders";
    }
}