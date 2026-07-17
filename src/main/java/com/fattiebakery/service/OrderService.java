package com.fattiebakery.service;

import com.fattiebakery.model.*;
import com.fattiebakery.repository.OrderRepository;
import com.fattiebakery.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class OrderService {

    @Autowired private OrderRepository orderRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private CartService cartService;
    @Autowired private DiscountCodeService discountCodeService;

    /**
     * Hàm tạo đơn hàng hoàn chỉnh
     */
    public Order createOrder(List<CartItem> cartItems, String customerName, String customerEmail,
                             String customerPhone, String shippingAddress,
                             String discountCodeStr, Order.PaymentMethod paymentMethod,
                             String notes, User user) {

        if (cartItems == null || cartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống bác Hải Nam ơi!");
        }

        // 1. Tính toán tiền nong
        BigDecimal totalAmount = cartService.getCartTotal(cartItems);
        BigDecimal discountAmount = BigDecimal.ZERO;
        DiscountCode discountCode = null;

        // 2. Xử lý mã giảm giá
        if (discountCodeStr != null && !discountCodeStr.isBlank()) {
            try {
                // Lấy số tiền được giảm
                discountAmount = discountCodeService.applyDiscount(discountCodeStr, totalAmount);
                // Tìm đối tượng DiscountCode để lưu vào Order
                discountCode = discountCodeService.findByCode(discountCodeStr).orElse(null);

                if (discountCode != null) {
                    discountCodeService.incrementUsage(discountCode);
                }
            } catch (Exception e) {
                // Nếu mã lỗi (hết hạn, ko đủ điều kiện) thì cho giảm về 0 để khách vẫn đặt được hàng
                discountAmount = BigDecimal.ZERO;
                System.err.println("Lỗi áp mã giảm giá: " + e.getMessage());
            }
        }

        BigDecimal finalAmount = totalAmount.subtract(discountAmount);

        // 3. Khởi tạo đối tượng Order (Dùng Setter cho an toàn nếu Builder của bác bị lỗi)
        Order order = new Order();
        order.setOrderCode(generateOrderCode());
        order.setUser(user);
        order.setCustomerName(customerName);
        order.setCustomerEmail(customerEmail);
        order.setCustomerPhone(customerPhone);
        order.setShippingAddress(shippingAddress);
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discountAmount);
        order.setFinalAmount(finalAmount);
        order.setDiscountCode(discountCode);
        order.setPaymentMethod(paymentMethod);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setPaymentStatus(Order.PaymentStatus.UNPAID);
        order.setNotes(notes);
        order.setCreatedAt(LocalDateTime.now());

        // 4. Tạo OrderItems và Cập nhật kho hàng
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            Product p = cartItem.getProduct();

            // Kiểm tra tồn kho
            if (p.getStockQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException("Sản phẩm " + p.getName() + " không đủ hàng bác ơi!");
            }

            // Cập nhật số lượng
            p.setSoldCount(p.getSoldCount() + cartItem.getQuantity());
            p.setStockQuantity(p.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(p);

            // Tạo Item con
            OrderItem oi = new OrderItem();
            oi.setOrder(order); // Gán cha cho con (Bắt buộc để tránh lỗi 500)
            oi.setProduct(p);
            oi.setProductName(p.getName());
            oi.setProductPrice(p.getPrice());
            oi.setQuantity(cartItem.getQuantity());
            oi.setSubtotal(p.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));

            orderItems.add(oi);
        }

        // 5. Gán danh sách con vào cha
        order.setOrderItems(orderItems);

        // 6. Lưu đơn hàng vào Database
        return orderRepository.save(order);
    }

    // --- CÁC HÀM BỔ TRỢ ---

    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Order updateOrderStatus(Long id, Order.OrderStatus newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));
        order.setStatus(newStatus);
        if (newStatus == Order.OrderStatus.DELIVERED) {
            order.setPaymentStatus(Order.PaymentStatus.PAID);
        }
        return orderRepository.save(order);
    }

    private String generateOrderCode() {
        return "KF" + System.currentTimeMillis();
    }

    // --- THỐNG KÊ ADMIN ---

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        BigDecimal totalRevenue = orderRepository.getTotalRevenue();
        stats.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        stats.put("pendingOrders", orderRepository.countByStatus(Order.OrderStatus.PENDING));
        stats.put("deliveredOrders", orderRepository.countByStatus(Order.OrderStatus.DELIVERED));
        return stats;
    }

    public Page<Order> adminSearchOrders(String status, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Order.OrderStatus statusEnum = null;
        if (status != null && !status.isBlank() && !status.equals("ALL")) {
            try {
                statusEnum = Order.OrderStatus.valueOf(status);
            } catch (Exception e) {
                System.out.println("Lỗi chuyển đổi status: " + e.getMessage());
            }
        }

        // Nếu bác chưa có hàm searchOrders trong OrderRepository,
        // tạm thời bác có thể dùng hàm mặc định của Spring Data JPA như sau:
        if (statusEnum != null) {
            return orderRepository.findByStatus(statusEnum, pageable);
        }

        return orderRepository.findAll(pageable);
    }

    public Optional<Order> findByOrderCode(String orderCode) {
        return orderRepository.findByOrderCode(orderCode);
    }

    public double[] getMonthlyRevenueData() {
        double[] monthlyData = new double[12]; // Mảng 12 phần tử toàn số 0
        List<Object[]> rawData = orderRepository.getMonthlyRevenueRaw();

        for (Object[] row : rawData) {
            int month = (int) row[0]; // Tháng (1-12)
            double revenue = ((Number) row[1]).doubleValue(); // Doanh thu
            if (month >= 1 && month <= 12) {
                monthlyData[month - 1] = revenue; // Gán vào đúng vị trí trong mảng
            }
        }
        return monthlyData;
    }
}