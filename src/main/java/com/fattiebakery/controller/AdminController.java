package com.fattiebakery.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.fattiebakery.model.*;
import com.fattiebakery.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.nio.file.*;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private ProductService productService;
    @Autowired private CategoryService categoryService;
    @Autowired private UserService userService;
    @Autowired private OrderService orderService;
    @Autowired private DiscountCodeService discountCodeService;

    @Value("${app.upload.dir:uploads/images}")
    private String uploadDir;

    // ==================== DASHBOARD ====================

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        // Lấy stats từ service
        java.util.Map<String, Object> stats = orderService.getDashboardStats();

        // PHÒNG THỦ: Đảm bảo các key quan trọng luôn tồn tại để tránh lỗi 500 ngoài Thymeleaf
        if (stats == null) stats = new java.util.HashMap<>();
        stats.putIfAbsent("monthRevenue", 0.0);
        stats.putIfAbsent("totalOrders", 0L);
        stats.putIfAbsent("pendingOrders", 0L);

        model.addAttribute("stats", stats);
        model.addAttribute("totalProducts", productService.countActiveProducts());
        model.addAttribute("totalUsers", userService.countActiveUsers());

        // Lấy danh sách sản phẩm sắp hết hàng (Stock < 10)
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
        model.addAttribute("searchName", name);
        model.addAttribute("searchCategory", categoryId);
        model.addAttribute("currentPage", page);
        return "admin/products";
    }

    @GetMapping("/products/new")
    public String newProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getActiveCategories());
        return "admin/product-form";
    }

    @GetMapping("/products/edit/{id}")
    public String editProductForm(@PathVariable Long id, Model model) {
        return productService.getProductById(id).map(p -> {
            model.addAttribute("product", p);
            model.addAttribute("categories", categoryService.getActiveCategories());
            return "admin/product-form";
        }).orElse("redirect:/admin/products");
    }

    @Autowired
    private Cloudinary cloudinary;
    /*
    @PostMapping("/products/save")
    public String saveProduct(@ModelAttribute Product product,
                              @RequestParam(required = false) MultipartFile imageFile,
                              @RequestParam(required = false) Long categoryId,
                              RedirectAttributes ra) {
        try {
            if (categoryId != null) {
                categoryService.getCategoryById(categoryId).ifPresent(product::setCategory);
            }

            if (imageFile != null && !imageFile.isEmpty()) {
                String filename = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Files.copy(imageFile.getInputStream(), uploadPath.resolve(filename),
                        StandardCopyOption.REPLACE_EXISTING);

                // SỬA TẠI ĐÂY: Lưu tên file thôi, đường dẫn /uploads/ sẽ do HTML hoặc WebConfig lo
                // Tránh việc lưu "//uploads//" gây lỗi Firewall bác nhé
                product.setImageUrl(filename);
            }

            productService.saveProduct(product);
            ra.addFlashAttribute("success", "Lưu sản phẩm thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }
    */

    @PostMapping("/products/save")
    public String saveProduct(@ModelAttribute Product product,
                              @RequestParam(required = false) MultipartFile imageFile,
                              @RequestParam(required = false) Long categoryId,
                              RedirectAttributes ra) {
        try {
            // 1. Gán Category (Giữ nguyên của bác)
            if (categoryId != null) {
                categoryService.getCategoryById(categoryId).ifPresent(product::setCategory);
            }

            // 2. Xử lý Upload ảnh lên Cloudinary
            if (imageFile != null && !imageFile.isEmpty()) {
                // Gửi file lên Cloudinary
                Map uploadResult = cloudinary.uploader().upload(imageFile.getBytes(),
                        ObjectUtils.asMap("resource_type", "auto"));

                // Lấy link URL tuyệt đối (https://res.cloudinary.com/...)
                String imageUrl = uploadResult.get("secure_url").toString();

                // Lưu link này vào Database luôn bác nhé
                product.setImageUrl(imageUrl);
            }

            productService.saveProduct(product);
            ra.addFlashAttribute("success", "Lưu sản phẩm lên mây thành công!");
        } catch (Exception e) {
            e.printStackTrace(); // In lỗi ra Logs Render để bác dễ soi bệnh
            ra.addFlashAttribute("error", "Lỗi upload: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }


    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes ra) {
        productService.deleteProduct(id);
        ra.addFlashAttribute("success", "Đã xóa sản phẩm!");
        return "redirect:/admin/products";
    }

    // ==================== CATEGORIES ====================

    @PostMapping("/categories/save")
    public String saveCategory(@ModelAttribute Category category, RedirectAttributes ra) {
        categoryService.saveCategory(category);
        ra.addFlashAttribute("success", "Lưu danh mục thành công!");
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes ra) {
        categoryService.deleteCategory(id);
        ra.addFlashAttribute("success", "Đã xóa danh mục!");
        return "redirect:/admin/categories";
    }

    // ==================== USERS ====================

    @GetMapping("/users")
    public String users(@RequestParam(required = false) String keyword,
                        @RequestParam(defaultValue = "0") int page,
                        Model model) {
        model.addAttribute("users", userService.searchUsers(keyword, page, 10));
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", page);
        return "admin/users";
    }

    @PostMapping("/users/toggle/{id}")
    public String toggleUser(@PathVariable Long id, RedirectAttributes ra) {
        userService.toggleUserStatus(id);
        ra.addFlashAttribute("success", "Cập nhật trạng thái người dùng thành công!");
        return "redirect:/admin/users";
    }

    // ==================== ORDERS ====================

    @GetMapping("/orders")
    public String orders(@RequestParam(required = false) String status,
                         @RequestParam(required = false) String keyword,
                         @RequestParam(defaultValue = "0") int page,
                         Model model) {
        model.addAttribute("orders", orderService.adminSearchOrders(status, keyword, page, 10));
        model.addAttribute("statusFilter", status);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", page);
        model.addAttribute("orderStatuses", Order.OrderStatus.values());
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

    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(@PathVariable Long id,
                                    @RequestParam String status,
                                    RedirectAttributes ra) {
        try {
            orderService.updateOrderStatus(id, Order.OrderStatus.valueOf(status));
            ra.addFlashAttribute("success", "Cập nhật trạng thái đơn hàng thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/orders/" + id;
    }

    // ==================== DISCOUNT CODES ====================

    @GetMapping("/discounts")
    public String discounts(Model model) {
        model.addAttribute("discounts", discountCodeService.getAllDiscountCodes());
        model.addAttribute("discount", new DiscountCode());
        return "admin/discounts";
    }

    @PostMapping("/discounts/save")
    public String saveDiscount(@ModelAttribute DiscountCode discountCode, RedirectAttributes ra) {
        discountCodeService.save(discountCode);
        ra.addFlashAttribute("success", "Lưu mã giảm giá thành công!");
        return "redirect:/admin/discounts";
    }

    @PostMapping("/discounts/delete/{id}")
    public String deleteDiscount(@PathVariable Long id, RedirectAttributes ra) {
        discountCodeService.delete(id);
        ra.addFlashAttribute("success", "Đã xóa mã giảm giá!");
        return "redirect:/admin/discounts";
    }

    // ==================== STATISTICS ====================

    // ==================== STATISTICS ====================

    @GetMapping("/statistics")
    public String statistics(Model model) {
        var stats = orderService.getDashboardStats();
        if (stats == null) stats = new java.util.HashMap<>();

        // Lấy dữ liệu thật từ Service
        double[] realRevenueData = orderService.getMonthlyRevenueData();

        // Nếu mảng null hoặc trống, tạo mảng 12 số 0 để JS không bị lỗi cú pháp
        if (realRevenueData == null || realRevenueData.length == 0) {
            realRevenueData = new double[12];
        }
        stats.put("monthlyRevenueList", realRevenueData);

        // Tính doanh thu tháng hiện tại
        int currentMonth = java.time.LocalDate.now().getMonthValue();
        stats.put("monthRevenue", realRevenueData[currentMonth - 1]);

        model.addAttribute("stats", stats);
        model.addAttribute("totalProducts", productService.countActiveProducts());
        model.addAttribute("totalUsers", userService.countActiveUsers());
        return "admin/statistics";
    }
}
