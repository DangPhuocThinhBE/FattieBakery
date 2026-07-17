package com.fattiebakery.controller;

import com.fattiebakery.model.*;
import com.fattiebakery.repository.CartItemRepository;
import com.fattiebakery.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    @Autowired private CartService cartService;
    @Autowired private OrderService orderService;
    @Autowired private UserService userService;
    @Autowired private DiscountCodeService discountCodeService; // Thêm cái này để tính tiền giảm
    @Autowired private CartItemRepository cartItemRepository; // Thêm dòng này để hết lỗi đỏ dòng 49

    @GetMapping
    public String checkoutPage(Model model,
                               Authentication auth,
                               HttpSession session,
                               @RequestParam(required = false) String itemIds, // Nhận danh sách ID món đồ đã chọn
                               @RequestParam(required = false) String coupon) {

        User user = null;
        if (auth != null && auth.isAuthenticated()) {
            user = userService.findByUsername(auth.getName()).orElse(null);
        }

        // LẤY SẢN PHẨM: Thay vì lấy hết, ta sẽ lọc theo itemIds
        List<CartItem> cartItems;
        if (itemIds != null && !itemIds.isBlank()) {
            // Chuyển chuỗi "5,7,8" thành danh sách List<Long>
            List<Long> ids = Arrays.stream(itemIds.split(","))
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
            // Bác cần thêm hàm này vào CartService hoặc dùng Repo trực tiếp
            cartItems = cartItemRepository.findAllById(ids);
        } else {
            // Nếu không có itemIds trên link, quay về giỏ hàng
            return "redirect:/cart";
        }

        if (cartItems.isEmpty()) return "redirect:/cart";

        // TÍNH TOÁN TIỀN NONG
        BigDecimal total = cartService.getCartTotal(cartItems);
        BigDecimal discountAmount = BigDecimal.ZERO;

        if (coupon != null && !coupon.isBlank()) {
            try {
                discountAmount = discountCodeService.applyDiscount(coupon, total);
            } catch (Exception e) {
                discountAmount = BigDecimal.ZERO;
            }
        }

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", total);
        model.addAttribute("discountAmount", discountAmount);
        model.addAttribute("appliedCoupon", coupon);
        model.addAttribute("user", user);

        return "user/checkout";
    }

    @PostMapping("/place-order")
    public String placeOrder(@RequestParam String customerName,
                             @RequestParam String customerPhone,
                             @RequestParam String customerEmail,
                             @RequestParam String shippingAddress,
                             @RequestParam(required = false) String discountCode,
                             @RequestParam String paymentMethod,
                             @RequestParam(required = false) String notes,
                             Principal principal,
                             RedirectAttributes ra) {
        try {
            if (principal == null) return "redirect:/login";

            User user = userService.findByUsername(principal.getName()).orElse(null);

            // Lấy giỏ hàng thực tế từ DB
            List<CartItem> cartItems = cartService.getCartItems(user, null);

            if (cartItems == null || cartItems.isEmpty()) {
                ra.addFlashAttribute("error", "Giỏ hàng của bác trống trơn rồi!");
                return "redirect:/cart";
            }

            // Gọi Service tạo đơn
            Order order = orderService.createOrder(
                    cartItems, customerName, customerEmail, customerPhone,
                    shippingAddress, discountCode,
                    Order.PaymentMethod.valueOf(paymentMethod),
                    notes, user
            );

            // Xóa giỏ hàng sau khi đặt thành công
            cartService.clearCart(user, null);

            ra.addFlashAttribute("success", "Đặt hàng thành công bác Hải Nam ơi! 🎉");
            return "redirect:/orders/" + order.getOrderCode(); // Chuyển sang trang chi tiết đơn hàng

        } catch (Exception e) {
            e.printStackTrace();
            ra.addFlashAttribute("error", "Lỗi đặt hàng: " + e.getMessage());
            return "redirect:/checkout";
        }
    }
}