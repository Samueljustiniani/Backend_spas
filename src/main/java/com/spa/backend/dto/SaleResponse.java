package com.spa.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
public class SaleResponse {
    private Long id;
    private OffsetDateTime saleDate;
    private BigDecimal total;
    private String paymentType;
    private String status;
    private Long userId;
    private String userName;
    private List<SaleDetailResponse> details;

    @Data
    @Builder
    public static class SaleDetailResponse {
        private Long id;
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
    }
}
