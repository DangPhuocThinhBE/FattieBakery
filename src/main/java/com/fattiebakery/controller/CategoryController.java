package com.fattiebakery.controller;

// IMPORT ĐÚNG CỦA SPRING (Bác thay thế dòng ch.qos.logback bằng dòng này)
import org.springframework.ui.Model;

import com.fattiebakery.model.Category;
import com.fattiebakery.service.CategoryService; // Bác nhớ thêm import Service
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller // Bác nhớ thêm Annotation này để Spring nhận diện nhé
public class CategoryController {

    @Autowired
    private CategoryService categoryService; // Phải khai báo biến này thì mới gọi được hàm bên dưới

    @GetMapping("/admin/categories")
    public String listCategories(Model model, @RequestParam(value = "editId", required = false) Long editId) {
        // Bây giờ model.addAttribute sẽ hoạt động đúng
        model.addAttribute("categories", categoryService.getAllCategories());

        if (editId != null) {
            // Nếu có editId, lấy danh mục đó để điền vào Form
            Category category = categoryService.getCategoryById(editId).orElse(null);
            model.addAttribute("category", category);
        } else {
            // Nếu không có, tạo một đối tượng trống để Thêm mới
            model.addAttribute("category", new Category());
        }
        return "admin/categories";
    }
}