package com.fattiebakery.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import java.math.BigDecimal;
import java.time.LocalDateTime; // Dùng LocalDateTime thay vì LocalDate

@Entity
@Table(name = "discount_codes")
@Data
public class DiscountCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    @Column(name = "discount_type")
    private String discountType; // PERCENTAGE hoặc FIXED_AMOUNT

    @Column(name = "discount_value")
    private BigDecimal discountValue;

    @Column(name = "minimum_order_amount")
    private BigDecimal minimumOrderAmount;

    @Column(name = "max_usage_count")
    private Integer maxUsageCount;

    @Column(name = "current_usage_count")
    private Integer currentUsageCount = 0;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endDate;

    private Boolean active;
}