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
    private Long serviceId;
    private String serviceName;
    private Long roomId;
    private String roomName;
}
