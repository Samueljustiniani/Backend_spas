package com.spa.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class SaleRequest {
    private Long userId;
    private String paymentType; // 'E' = Efectivo, 'T' = Tarjeta
    private List<SaleDetailRequest> details;

    @Data
    public static class SaleDetailRequest {
        private Long productId;
        private Integer quantity;
    }
}
