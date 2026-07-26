package com.fattiebakery.controller;

import com.fattiebakery.model.Category;
import com.fattiebakery.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class HomeController {

    @Autowired private ProductService productService;
    @Autowired private CategoryService categoryService;

    // Tự động thêm danh mục vào tất cả các view trả về từ Controller này
    @ModelAttribute("categories")
    public List<Category> getCategories() {
        return categoryService.getActiveCategories();
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("newestProducts", productService.getNewestProducts());
        model.addAttribute("bestSellingProducts", productService.getBestSellingProducts());
        model.addAttribute("featuredProducts", productService.getFeaturedProducts());
        return "user/home";
    }

    @GetMapping("/shop")
    public String shop(@RequestParam(required = false) Long category,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "8") int size, // Nhận thêm tham số size, mặc định là 12
                       Model model) {

        if (category != null) {
            model.addAttribute("products", productService.getProductsByCategory(category, page, size));
            model.addAttribute("selectedCategory", categoryService.getCategoryById(category).orElse(null));
        } else {
            model.addAttribute("products", productService.getAllActiveProducts(page, size));
        }

        model.addAttribute("currentPage", page);
        model.addAttribute("size", size); // Truyền ngược biến size ra View để giữ trạng thái select option
        return "user/shop";
    }

    @GetMapping("/search")
    public String search(@RequestParam String keyword,
                         @RequestParam(defaultValue = "0") int page,
                         Model model) {
        model.addAttribute("products", productService.searchProducts(keyword, page, 12));
        model.addAttribute("keyword", keyword);
        return "user/search";
    }

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        return productService.getProductById(id).map(p -> {
            model.addAttribute("product", p);
            // Kiểm tra category tồn tại trước khi gọi service
            if (p.getCategory() != null) {
                model.addAttribute("relatedProducts",
                        productService.getProductsByCategory(p.getCategory().getId(), 0, 4).getContent());
            }
            return "user/product-detail";
        }).orElse("redirect:/shop");
    }

    // Các trang tĩnh (Story, About, Contact)
    @GetMapping({"/story", "/about", "/contact"})
    public String staticPages(jakarta.servlet.http.HttpServletRequest request) {
        String path = request.getRequestURI().replace("/", "");
        if (path.isEmpty()) {
            return "redirect:/";
        }
        return "user/" + path;
    }
}