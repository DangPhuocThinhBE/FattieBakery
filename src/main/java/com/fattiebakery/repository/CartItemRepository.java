package com.fattiebakery.repository;

import com.fattiebakery.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByUserId(Long userId);
    List<CartItem> findBySessionId(String sessionId);

    Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId);
    Optional<CartItem> findBySessionIdAndProductId(String sessionId, Long productId);

    void deleteByUserId(Long userId);
    void deleteBySessionId(String sessionId);

    long countByUserId(Long userId);
    long countBySessionId(String sessionId);

    @Query("SELECT SUM(ci.quantity) FROM CartItem ci WHERE ci.user.id = :userId")
    Long sumQuantityByUserId(@Param("userId") Long userId);

    @Query("SELECT SUM(ci.quantity) FROM CartItem ci WHERE ci.sessionId = :sessionId")
    Long sumQuantityBySessionId(@Param("sessionId") String sessionId);
}
