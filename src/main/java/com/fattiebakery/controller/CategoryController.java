package com.fattiebakery.controller;

// IMPORT ĐÚNG CỦA SPRING (Bác thay thế dòng ch.qos.logback bằng dòng này)
import com.fattiebakery.service.OrderService;
import org.springframework.ui.Model;

import com.fattiebakery.model.Category;
import com.fattiebakery.service.CategoryService; // Bác nhớ thêm import Service
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CategoryController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CategoryService categoryService;

    // Thêm phương thức này vào AdminController.java
    @GetMapping("/categories")
    public String listCategories(Model model, @RequestParam(value = "editId", required = false) Long editId) {
        // Đã có sẵn orderService và categoryService được inject qua Constructor
        model.addAttribute("stats", orderService.getDashboardStats());
        model.addAttribute("categories", categoryService.getAllCategories());

        if (editId != null) {
            model.addAttribute("category", categoryService.getCategoryById(editId).orElse(new Category()));
        } else {
            model.addAttribute("category", new Category());
        }
        return "admin/categories";
    }
}