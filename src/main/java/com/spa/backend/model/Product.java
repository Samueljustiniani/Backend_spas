package com.spa.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_product")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "price", nullable = false)
    private java.math.BigDecimal price;

    @Column(name = "stock", nullable = false)
    private Integer stock;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "status")
    private String status;
}
