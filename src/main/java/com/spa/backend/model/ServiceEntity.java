package com.spa.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "services")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_service")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "price", nullable = false)
    private java.math.BigDecimal price;

    @Column(name = "gender", nullable = false)
    private String gender; // "M" = Masculino, "F" = Femenino, "U" = Unisex

    @Column(name = "status", nullable = false)
    private String status; // "A" = Activo, "I" = Inactivo
}
