package com.spa.backend.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class QuoteRequest {
    private Long userId;
    private Long serviceId;
    private Long roomId;
    private LocalDate quoteDate;
    private LocalTime startTime;
    private LocalTime endTime;
}
