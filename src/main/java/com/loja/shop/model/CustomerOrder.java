package com.loja.shop.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")   // "order" é palavra reservada
@Getter @Setter @NoArgsConstructor
public class CustomerOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private AppUser user;
    private Integer totalItemCount;
    private String deliveryType;
    private BigDecimal deliveryTypeCost;
    private BigDecimal costBeforeDelivery;
    private BigDecimal costAfterDelivery;
    private String promoCode;
    private String contactNumber;
    private String status = "pending";
    private Instant createdAt = Instant.now();
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
}
