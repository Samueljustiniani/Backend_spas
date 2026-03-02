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
    
    /**
     * Obtiene los slots disponibles para una sala en una fecha dada,
     * considerando la duración total de los servicios seleccionados.
     * @param roomId ID de la sala
     * @param date Fecha de la cita
     * @param serviceIds Lista de IDs de servicios seleccionados
     * @return Lista de horas de inicio disponibles
     */
    java.util.List<java.time.LocalTime> getAvailableSlots(Long roomId, LocalDate date, java.util.List<Long> serviceIds);
    
    /**
     * Calcula la duración total y precio estimado para una lista de servicios
     */
    java.util.Map<String, Object> calculateEstimate(java.util.List<Long> serviceIds);
    
    /**
     * Marca citas pendientes como inactivas automáticamente
     */
    void markPendingQuotesInactive();

    /**
     * Reactiva una cita si el horario está libre
     */
    QuoteResponse reactivateQuote(Long quoteId);

    /**
     * Verifica si un usuario puede acceder a las citas del userId
     */
    boolean canUserAccessQuotes(Long userId, String email);
}
