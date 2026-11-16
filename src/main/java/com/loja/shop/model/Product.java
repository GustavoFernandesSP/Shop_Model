package com.loja.shop.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter @Setter @NoArgsConstructor
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("_id")   // o front usa _id
    private Long id;
    private String name;
    @Column(length = 1000)
    private String description;
    @Column(nullable = false)
    private BigDecimal price;
    @JsonProperty("product_image")   // o front usa product_image
    private String image;
    private String category;
}