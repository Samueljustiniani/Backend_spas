package com.spa.backend.service;

import com.spa.backend.dto.QuoteRequest;
import com.spa.backend.dto.QuoteResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface QuoteService {
    List<QuoteResponse> findAll();
    Optional<QuoteResponse> findById(Long id);
    QuoteResponse create(QuoteRequest request);
    QuoteResponse updateStatus(Long id, String status);
    void deleteById(Long id);
    List<QuoteResponse> findByUserId(Long userId);
    List<QuoteResponse> findByDate(LocalDate date);
    boolean isTimeSlotAvailable(Long roomId, LocalDate date, java.time.LocalTime startTime, java.time.LocalTime endTime);
}
