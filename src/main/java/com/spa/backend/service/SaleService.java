package com.spa.backend.service;

import com.spa.backend.dto.SaleRequest;
import com.spa.backend.dto.SaleResponse;

import java.util.List;
import java.util.Optional;

public interface SaleService {
    List<SaleResponse> findAll();
    Optional<SaleResponse> findById(Long id);
    SaleResponse create(SaleRequest request);
    SaleResponse updateStatus(Long id, String status);
    void deleteById(Long id);
    List<SaleResponse> findByUserId(Long userId);
}
