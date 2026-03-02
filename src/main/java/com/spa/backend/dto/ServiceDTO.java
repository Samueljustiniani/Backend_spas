package com.spa.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceDTO {
    private Long id;
    private String name;
    private String description;
    private Integer durationMinutes;
    private String gender; // "M" = Masculino, "F" = Femenino, "U" = Unisex
    private String imageUrl;
    private String status; // "A" = Activo, "I" = Inactivo
}