package com.fattiebakery.service;

import com.fattiebakery.model.CartItem;
import com.fattiebakery.model.Product;
import com.fattiebakery.model.User;
import com.fattiebakery.repository.CartItemRepository;
import com.fattiebakery.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CartService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    // Lấy giỏ hàng theo user hoặc session
    public List<CartItem> getCartItems(User user, String sessionId) {
        if (user != null) return cartItemRepository.findByUserId(user.getId());
        return cartItemRepository.findBySessionId(sessionId);
    }

    // Thêm vào giỏ hàng
    public CartItem addToCart(Long productId, int quantity, User user, String sessionId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        Optional<CartItem> existing = (user != null)
                ? cartItemRepository.findByUserIdAndProductId(user.getId(), productId)
                : cartItemRepository.findBySessionIdAndProductId(sessionId, productId);

        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + quantity);
            return cartItemRepository.save(item);
        } else {
            CartItem item = CartItem.builder()
                    .product(product)
                    .quantity(quantity)
                    .user(user)
                    .sessionId(user == null ? sessionId : null)
                    .build();
            return cartItemRepository.save(item);
        }
    }

    // Cập nhật số lượng
    public void updateQuantity(Long cartItemId, int quantity) {
        cartItemRepository.findById(cartItemId).ifPresent(item -> {
            if (quantity <= 0) {
                cartItemRepository.delete(item);
            } else {
                item.setQuantity(quantity);
                cartItemRepository.save(item);
            }
        });
    }

    // Xóa item
    public void removeItem(Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }

    // Xóa toàn bộ giỏ hàng
    public void clearCart(User user, String sessionId) {
        if (user != null) cartItemRepository.deleteByUserId(user.getId());
        else cartItemRepository.deleteBySessionId(sessionId);
    }

    // Tính tổng tiền
    public BigDecimal getCartTotal(List<CartItem> items) {
        return items.stream()
                .map(i -> i.getProduct().getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Số lượng item trong giỏ
    public long getCartCount(User user, String sessionId) {
        Long totalQuantity = 0L;

        if (user != null) {
            totalQuantity = cartItemRepository.sumQuantityByUserId(user.getId());
        } else if (sessionId != null) {
            totalQuantity = cartItemRepository.sumQuantityBySessionId(sessionId);
        }

        // Nếu totalQuantity là null (giỏ hàng trống) thì trả về 0, ngược lại trả về giá trị thực
        return totalQuantity != null ? totalQuantity : 0L;
    }

    // Chuyển giỏ hàng từ session sang user (sau khi đăng nhập)
    public void mergeCart(String sessionId, User user) {
        List<CartItem> sessionItems = cartItemRepository.findBySessionId(sessionId);
        for (CartItem item : sessionItems) {
            Optional<CartItem> existing = cartItemRepository
                    .findByUserIdAndProductId(user.getId(), item.getProduct().getId());
            if (existing.isPresent()) {
                existing.get().setQuantity(existing.get().getQuantity() + item.getQuantity());
                cartItemRepository.save(existing.get());
                cartItemRepository.delete(item);
            } else {
                item.setUser(user);
                item.setSessionId(null);
                cartItemRepository.save(item);
            }
        }
    }
}
