package com.fattiebakery.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor; // Thêm dòng này
import lombok.AllArgsConstructor; // Thêm dòng này
import org.springframework.format.annotation.DateTimeFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "discount_codes")
@Data
@NoArgsConstructor // CẦN THIẾT cho việc khởi tạo form
@AllArgsConstructor // Tốt cho việc tạo object nhanh
public class DiscountCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    @Column(name = "discount_type")
    private String discountType;

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

    private Boolean active = true; // Gán giá trị mặc định tránh lỗi null
}