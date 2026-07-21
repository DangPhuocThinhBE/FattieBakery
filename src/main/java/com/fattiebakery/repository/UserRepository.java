package com.fattiebakery.repository;

import com.fattiebakery.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE " +
            "(:keyword IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%',:keyword,'%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%',:keyword,'%')) " +
            "OR LOWER(u.fullName) LIKE LOWER(CONCAT('%',:keyword,'%')))")
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);

    long countByActiveTrue();

    // --- BỔ SUNG HÀM LỌC KHÁCH HÀNG THEO NGÀY SINH ---
    @Query("SELECT u FROM User u WHERE DAY(u.dateOfBirth) = :day AND MONTH(u.dateOfBirth) = :month")
    List<User> findByDayAndMonthOfBirth(@Param("day") int day, @Param("month") int month);
}