package com.fattiebakery.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.fattiebakery.model.*;
import com.fattiebakery.service.*;
import lombok.Getter;
import lombok.Setter;
// QUAN TRỌNG: Import đúng chuẩn Spring Data
import org.springframework.data.domain.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private ProductService productService;
    @Autowired private CategoryService categoryService;
    @Autowired private UserService userService;
    @Autowired private OrderService orderService;
    @Autowired private DiscountCodeService discountCodeService;

    @Setter @Getter
    @Value("${app.upload.dir:uploads/images}")
    private String uploadDir;

    // ==================== DASHBOARD ====================
    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        Map<String, Object> stats = orderService.getDashboardStats();
        if (stats == null) stats = new java.util.HashMap<>();
        stats.putIfAbsent("monthRevenue", 0.0);
        stats.putIfAbsent("totalOrders", 0L);
        stats.putIfAbsent("pendingOrders", 0L);

        model.addAttribute("stats", stats);
        model.addAttribute("totalProducts", productService.countActiveProducts());
        model.addAttribute("totalUsers", userService.countActiveUsers());
        model.addAttribute("lowStockProducts", productService.getLowStockProducts());
        return "admin/dashboard";
    }

    // ==================== PRODUCTS ====================
    @GetMapping("/products")
    public String products(@RequestParam(required = false) String name,
                           @RequestParam(required = false) Long categoryId,
                           @RequestParam(defaultValue = "0") int page,
                           Model model) {
        model.addAttribute("products", productService.adminSearchProducts(name, categoryId, page, 10));
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/products";
    }

    // [GIỮ NGUYÊN CÁC METHOD CŨ: newProductForm, editProductForm, saveProduct, deleteProduct]
    // ... (Để tiết kiệm không gian, bạn giữ nguyên các đoạn code cũ của bạn ở đây) ...

    // ==================== CATEGORIES (BẢN FULL ĐÃ FIX LỖI) ====================

    // 1. Hiển thị danh sách
    @GetMapping("/categories")
    public String listCategories(Model model) {
        // 1. Lấy dữ liệu cho sidebar/header thống kê
        Map<String, Object> stats = orderService.getDashboardStats();
        if (stats == null) stats = new java.util.HashMap<>();
        stats.putIfAbsent("monthRevenue", 0.0);
        stats.putIfAbsent("totalOrders", 0L);

        // 2. Truyền vào model để các trang dùng chung (layout) không bị lỗi
        model.addAttribute("stats", stats);
        model.addAttribute("lowStockProducts", productService.getLowStockProducts());
        // 3. Dữ liệu chính cho trang Categories
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("category", new Category());

        return "admin/categories";
    }

    // 2. Xử lý lưu (Thêm mới hoặc Cập nhật)
    @PostMapping("/categories/save")
    public String saveCategory(@ModelAttribute("category") Category category, RedirectAttributes ra) {
        try {
            categoryService.saveCategory(category);
            ra.addFlashAttribute("success", "Lưu danh mục thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi lưu danh mục: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    // 3. Xử lý sửa (Dùng cho nút Edit)
    @GetMapping("/categories/edit/{id}")
    public String editCategory(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return categoryService.getCategoryById(id).map(c -> {
            model.addAttribute("category", c);
            model.addAttribute("categories", categoryService.getAllCategories());
            return "admin/categories";
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "Không tìm thấy danh mục!");
            return "redirect:/admin/categories";
        });
    }

    // 4. Xử lý xóa
    @PostMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes ra) {
        try {
            categoryService.deleteCategory(id);
            ra.addFlashAttribute("success", "Đã xóa danh mục!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Không thể xóa danh mục này (có thể do đang chứa sản phẩm)!");
        }
        return "redirect:/admin/categories";
    }

    // ==================== USERS ====================
    @GetMapping("/users")
    public String users(@RequestParam(required = false) String keyword,
                        @RequestParam(defaultValue = "0") int page,
                        Model model) {
        model.addAttribute("users", userService.searchUsers(keyword, page, 10));
        return "admin/users";
    }

    // ==================== ORDERS (ĐÃ FIX LỖI 500) ====================
    @GetMapping("/orders")
    public String orders(@RequestParam(defaultValue = "0") int page, Model model) {
        // Đã sửa thành org.springframework.data.domain.Page
        Page<Order> orderPage = (Page<Order>) orderService.adminSearchOrders(null, null, page, 10);
        model.addAttribute("orders", orderPage);
        return "admin/orders";
    }

    // ==================== STATISTICS (Đã gộp để tránh trùng lặp URL) ====================
    @GetMapping("/statistics")
    public String statistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        Map<String, Object> stats = orderService.getStatisticsByDateRange(startDate, endDate);

        // Bổ sung các thông tin phụ trợ cho giao diện báo cáo doanh thu
        model.addAttribute("totalProducts", productService.countActiveProducts());
        model.addAttribute("totalUsers", userService.countActiveUsers());

        // Format định dạng tiền tệ tổng doanh thu ra chuỗi hiển thị đẹp mắt
        BigDecimal totalRev = (BigDecimal) stats.get("totalRevenue");
        model.addAttribute("revenueFormatted", totalRev != null ?
                String.format("%,d", totalRev.longValue()).replace(',', '.') : "0");

        model.addAttribute("stats", stats);
        return "admin/statistics";
    }

    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable Long id, Model model) {
        return orderService.findById(id).map(o -> {
            model.addAttribute("order", o);
            model.addAttribute("orderStatuses", Order.OrderStatus.values());
            return "admin/order-detail";
        }).orElse("redirect:/admin/orders");
    }


    @GetMapping
    public String viewDiscountsPage(Model model) {
        // Khớp với hàm getAllDiscountCodes() trong Service của bạn
        model.addAttribute("discounts", discountCodeService.getAllDiscountCodes());
        model.addAttribute("discount", new DiscountCode());
        return "admin/discounts";
    }

    @PostMapping("/save")
    public String saveDiscount(@ModelAttribute("discount") DiscountCode discountCode) {
        // Khớp với hàm save() trong Service của bạn
        discountCodeService.save(discountCode);
        return "redirect:/admin/discounts";
    }
}

    // ==================== KHÁC ====================
    // [GIỮ NGUYÊN PHẦN: DISCOUNT CODES, STATISTICS]
