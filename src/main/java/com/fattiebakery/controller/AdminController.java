package com.fattiebakery.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.fattiebakery.model.*;
import com.fattiebakery.service.*;
import lombok.Getter;
import lombok.Setter;

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

    @GetMapping("/products/new")
    public String newProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/product-form";
    }

    @GetMapping("/products/edit/{id}")
    public String editProductForm(@PathVariable("id") Long id, Model model, RedirectAttributes ra) {
        return productService.getProductById(id).map(product -> {
            model.addAttribute("product", product);
            model.addAttribute("categories", categoryService.getAllCategories());
            return "admin/product-form";
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "Không tìm thấy sản phẩm!");
            return "redirect:/admin/products";
        });
    }

    @PostMapping("/products/save")
    public String saveProduct(@ModelAttribute("product") Product product,
                              @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                              RedirectAttributes ra) {
        try {
            productService.saveProduct(product, imageFile);
            ra.addFlashAttribute("success", "Lưu sản phẩm thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi lưu sản phẩm: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long id, RedirectAttributes ra) {
        try {
            productService.deleteProduct(id);
            ra.addFlashAttribute("success", "Đã xóa sản phẩm!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Không thể xóa sản phẩm này!");
        }
        return "redirect:/admin/products";
    }

    // ==================== CATEGORIES ====================

    @GetMapping("/categories")
    public String listCategories(Model model) {
        Map<String, Object> stats = orderService.getDashboardStats();
        if (stats == null) stats = new java.util.HashMap<>();
        stats.putIfAbsent("monthRevenue", 0.0);
        stats.putIfAbsent("totalOrders", 0L);

        model.addAttribute("stats", stats);
        model.addAttribute("lowStockProducts", productService.getLowStockProducts());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("category", new Category());

        return "admin/categories";
    }

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

    // ==================== USERS MANAGEMENT ====================
    @GetMapping("/users")
    public String users(@RequestParam(required = false) String keyword,
                        @RequestParam(defaultValue = "0") int page,
                        Model model) {
        model.addAttribute("users", userService.searchUsers(keyword, page, 10));
        return "admin/users";
    }

    @PostMapping("/users/toggle/{id}")
    public String toggleUserStatus(@PathVariable("id") Long id, RedirectAttributes ra) {
        try {
            userService.toggleUserStatus(id);
            ra.addFlashAttribute("success", "Thay đổi trạng thái tài khoản thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/role/{id}")
    public String toggleUserRole(@PathVariable("id") Long id, RedirectAttributes ra) {
        try {
            userService.toggleUserRole(id);
            ra.addFlashAttribute("success", "Cập nhật quyền thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi phân quyền: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // ==================== ORDERS ====================
    @GetMapping("/orders")
    public String adminOrders(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Page<Order> orderPage = orderService.adminSearchOrders(keyword, status, page, 20);
        model.addAttribute("orders", orderPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("statusFilter", status);
        return "admin/orders";
    }

    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable Long id, Model model) {
        return orderService.findById(id).map(o -> {
            model.addAttribute("order", o);
            model.addAttribute("orderStatuses", Order.OrderStatus.values());
            return "admin/order-detail";
        }).orElse("redirect:/admin/orders");
    }

    // Cập nhật trạng thái đơn hàng từ trang chi tiết
    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(@PathVariable("id") Long id,
                                    @RequestParam("status") Order.OrderStatus status,
                                    RedirectAttributes ra) {
        try {
            orderService.updateOrderStatus(id, status);
            ra.addFlashAttribute("success", "Cập nhật trạng thái đơn hàng thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi cập nhật trạng thái: " + e.getMessage());
        }
        return "redirect:/admin/orders";
    }

    // ==================== STATISTICS ====================
    @GetMapping("/statistics")
    public String statistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        Map<String, Object> stats = orderService.getStatisticsByDateRange(startDate, endDate);

        model.addAttribute("totalProducts", productService.countActiveProducts());
        model.addAttribute("totalUsers", userService.countActiveUsers());

        BigDecimal totalRev = (BigDecimal) stats.get("totalRevenue");
        model.addAttribute("revenueFormatted", totalRev != null ?
                String.format("%,d", totalRev.longValue()).replace(',', '.') : "0");

        model.addAttribute("stats", stats);
        return "admin/statistics";
    }

    /*// ==================== DISCOUNTS ====================
    @GetMapping("/discounts")
    public String viewDiscountsPage(Model model) {
        model.addAttribute("discounts", discountCodeService.getAllDiscountCodes());
        model.addAttribute("discount", new DiscountCode());
        return "admin/discounts";
    }

    @PostMapping("/discounts/save")
    public String saveDiscount(@ModelAttribute("discount") DiscountCode discountCode) {
        discountCodeService.save(discountCode);
        return "redirect:/admin/discounts";
    }*/
}