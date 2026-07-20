package com.fattiebakery.service;

import com.fattiebakery.model.*;
import com.fattiebakery.repository.OrderRepository;
import com.fattiebakery.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        CartService cartService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.cartService = cartService;
    }

    // ==================== XỬ LÝ ĐƠN HÀNG ====================

    public Order createOrder(List<CartItem> cartItems, String customerName, String customerEmail,
                             String customerPhone, String shippingAddress, String discountCodeStr,
                             Order.PaymentMethod paymentMethod, String notes, User user) {

        BigDecimal totalAmount = cartService.getCartTotal(cartItems);

        Order order = new Order();
        order.setOrderCode("KF" + System.currentTimeMillis());
        order.setUser(user);
        order.setCustomerName(customerName);
        order.setCustomerEmail(customerEmail);
        order.setCustomerPhone(customerPhone);
        order.setShippingAddress(shippingAddress);
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setFinalAmount(totalAmount);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setPaymentMethod(paymentMethod);
        order.setNotes(notes);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        List<OrderItem> items = new ArrayList<>();
        for (CartItem ci : cartItems) {
            Product p = ci.getProduct();
            if (p.getStockQuantity() < ci.getQuantity()) {
                throw new RuntimeException("Sản phẩm " + p.getName() + " không đủ hàng!");
            }

            p.setStockQuantity(p.getStockQuantity() - ci.getQuantity());
            p.setSoldCount(p.getSoldCount() + ci.getQuantity());
            productRepository.save(p);

            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProduct(p);
            oi.setProductName(p.getName());
            oi.setQuantity(ci.getQuantity());
            oi.setProductPrice(p.getPrice());
            oi.setSubtotal(p.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
            items.add(oi);
        }
        order.setOrderItems(items);
        return orderRepository.save(order);
    }

    // ==================== ADMIN & DASHBOARD ====================

    public Page<Order> adminSearchOrders(String statusStr, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Order.OrderStatus status = null;
        if (statusStr != null && !statusStr.isEmpty()) {
            try { status = Order.OrderStatus.valueOf(statusStr); } catch (Exception ignored) {}
        }
        return orderRepository.searchOrders(status, keyword, pageable);
    }

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders", orderRepository.count());
        stats.put("pendingOrders", orderRepository.countByStatus(Order.OrderStatus.PENDING));
        stats.put("monthRevenue", orderRepository.getTotalRevenue());
        return stats;
    }

    public void updateOrderStatus(Long id, Order.OrderStatus orderStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng ID: " + id));
        order.setStatus(orderStatus);
        orderRepository.save(order);
    }

    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    public Optional<Order> findByOrderCode(String orderCode) {
        return orderRepository.findByOrderCode(orderCode);
    }

    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public double[] getMonthlyRevenueData() {
        List<Object[]> rawData = orderRepository.getMonthlyRevenueRaw();
        double[] revenueData = new double[12];
        for (Object[] row : rawData) {
            int month = ((Number) row[0]).intValue();
            double amount = ((BigDecimal) row[1]).doubleValue();
            if (month >= 1 && month <= 12) {
                revenueData[month - 1] = amount;
            }
        }
        return revenueData;
    }
}