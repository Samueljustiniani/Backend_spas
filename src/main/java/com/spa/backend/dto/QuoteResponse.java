package com.spa.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class QuoteResponse {
    private Long id;
    private LocalDate quoteDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status;
    private Long userId;
    private String userName;
    private java.util.List<Long> serviceIds;
    private java.util.List<String> serviceNames;
    private Long roomId;
    private String roomName;
    private Integer totalDurationMinutes;
    private java.math.BigDecimal totalPrice;
}
