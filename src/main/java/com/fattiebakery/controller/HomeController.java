package com.fattiebakery.controller;

import com.fattiebakery.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class HomeController {

    @Autowired private ProductService productService;
    @Autowired private CategoryService categoryService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("newestProducts", productService.getNewestProducts());
        model.addAttribute("bestSellingProducts", productService.getBestSellingProducts());
        model.addAttribute("featuredProducts", productService.getFeaturedProducts());
        model.addAttribute("categories", categoryService.getActiveCategories());
        return "user/home";
    }

    @GetMapping("/shop")
    public String shop(@RequestParam(required = false) Long category,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        int size = 12;
        if (category != null) {
            model.addAttribute("products", productService.getProductsByCategory(category, page, size));
            model.addAttribute("selectedCategory", categoryService.getCategoryById(category).orElse(null));
        } else {
            model.addAttribute("products", productService.getAllActiveProducts(page, size));
        }
        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("currentPage", page);
        return "user/shop";
    }

    @GetMapping("/search")
    public String search(@RequestParam String keyword,
                         @RequestParam(defaultValue = "0") int page,
                         Model model) {
        model.addAttribute("products", productService.searchProducts(keyword, page, 12));
        model.addAttribute("keyword", keyword);
        model.addAttribute("categories", categoryService.getActiveCategories());
        return "user/search";
    }

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        return productService.getProductById(id).map(p -> {
            model.addAttribute("product", p);
            model.addAttribute("categories", categoryService.getActiveCategories());
            model.addAttribute("relatedProducts",
                    productService.getProductsByCategory(p.getCategory() != null ? p.getCategory().getId() : null, 0, 4).getContent());
            return "user/product-detail";
        }).orElse("redirect:/shop");
    }
    // Điều hướng cho trang Chuyện của bé
    @GetMapping("/story")
    public String storyPage() {
        return "user/story"; // Thêm "user/" vào trước
    }

    // Điều hướng cho trang Giới thiệu
    @GetMapping("/about")
    public String aboutPage() {
        return "user/about"; // Thêm "user/" vào trước
    }

    // Điều hướng cho trang Liên hệ
    @GetMapping("/contact")
    public String contactPage() {
        return "user/contact"; // Thêm "user/" vào trước
    }
}
