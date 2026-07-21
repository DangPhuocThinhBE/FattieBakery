package com.fattiebakery.controller;

import com.fattiebakery.model.DiscountCode;
import com.fattiebakery.service.DiscountCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/discounts")
public class DiscountAdminController {

    @Autowired
    private DiscountCodeService discountCodeService;

    // 1. Phục vụ hiển thị giao diện HTML khi truy cập: http://localhost:8081/admin/discounts
    @GetMapping
    public String viewDiscountsPage(Model model) {
        model.addAttribute("discounts", discountCodeService.getAllDiscountCodes());
        model.addAttribute("discount", new DiscountCode());
        return "admin/discounts"; // Trỏ đúng tới file templates/admin/discounts.html của bạn
    }

    // 2. Phục vụ lưu/cập nhật dữ liệu từ form trong file HTML gửi lên
    @PostMapping("/save")
    public String saveDiscount(@ModelAttribute("discount") DiscountCode discountCode) {
        discountCodeService.save(discountCode);
        return "redirect:/admin/discounts";
    }
}