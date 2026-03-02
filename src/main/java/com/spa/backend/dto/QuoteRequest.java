package com.spa.backend.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class QuoteRequest {
    private Long userId;
    private java.util.List<Long> serviceIds;
    private Long roomId;
    private LocalDate quoteDate;
    private LocalTime startTime;
    // endTime se calcula automáticamente según la duración de los servicios
    // totalDurationMinutes y totalPrice se calculan en el backend
}
