package com.fattiebakery.controller;

import com.fattiebakery.model.User;
import com.fattiebakery.service.OrderService;
import com.fattiebakery.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/orders")
public class OrderController {

    @Autowired private OrderService orderService;
    @Autowired private UserService userService;

    private User getCurrentUser(Authentication auth) {
        return userService.findByUsername(auth.getName()).orElseThrow();
    }

    @GetMapping
    public String myOrders(Model model, Authentication auth) {
        User user = getCurrentUser(auth);
        model.addAttribute("orders", orderService.getUserOrders(user.getId()));
        return "user/my-orders";
    }

    @GetMapping("/{orderCode}")
    public String orderDetail(@PathVariable String orderCode, Model model, Authentication auth) {
        return orderService.findByOrderCode(orderCode).map(order -> {
            model.addAttribute("order", order);
            return "user/order-detail";
        }).orElse("redirect:/orders");
    }
}
