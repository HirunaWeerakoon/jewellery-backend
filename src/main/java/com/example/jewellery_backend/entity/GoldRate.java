package com.example.jewellery_backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "gold_rate_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoldRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @Column(name = "rate", precision = 10, scale = 4, nullable = false)
    private BigDecimal rate;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Optional convenience constructor
    public GoldRate(BigDecimal rate, LocalDate effectiveDate) {
        this.rate = rate;
        this.effectiveDate = effectiveDate;
    }
}
