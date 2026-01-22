package com.spa.backend.service.impl;

import com.spa.backend.dto.QuoteRequest;
import com.spa.backend.dto.QuoteResponse;
import com.spa.backend.model.Quote;
import com.spa.backend.model.Room;
import com.spa.backend.model.ServiceEntity;
import com.spa.backend.model.User;
import com.spa.backend.repository.QuoteRepository;
import com.spa.backend.repository.RoomRepository;
import com.spa.backend.repository.ServiceRepository;
import com.spa.backend.repository.UserRepository;
import com.spa.backend.service.QuoteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class QuoteServiceImpl implements QuoteService {

    private final QuoteRepository quoteRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final RoomRepository roomRepository;

    public QuoteServiceImpl(QuoteRepository quoteRepository, UserRepository userRepository,
                           ServiceRepository serviceRepository, RoomRepository roomRepository) {
        this.quoteRepository = quoteRepository;
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
        this.roomRepository = roomRepository;
    }

    @Override
    public List<QuoteResponse> findAll() {
        return quoteRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<QuoteResponse> findById(Long id) {
        return quoteRepository.findById(id).map(this::toResponse);
    }

    @Override
    @Transactional
    public QuoteResponse create(QuoteRequest request) {
        // Validar disponibilidad del horario
        if (!isTimeSlotAvailable(request.getRoomId(), request.getQuoteDate(), 
                request.getStartTime(), request.getEndTime())) {
            throw new RuntimeException("El horario seleccionado no está disponible");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        ServiceEntity service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Sala no encontrada"));

        Quote quote = new Quote();
        quote.setUser(user);
        quote.setService(service);
        quote.setRoom(room);
        quote.setQuoteDate(request.getQuoteDate());
        quote.setStartTime(request.getStartTime());
        quote.setEndTime(request.getEndTime());
        quote.setStatus("P"); // P = Pendiente

        Quote saved = quoteRepository.save(quote);
        return toResponse(saved);
    }

    @Override
    public QuoteResponse updateStatus(Long id, String status) {
        return quoteRepository.findById(id).map(quote -> {
            quote.setStatus(status);
            return toResponse(quoteRepository.save(quote));
        }).orElse(null);
    }

    @Override
    public void deleteById(Long id) {
        quoteRepository.findById(id).ifPresent(quote -> {
            quote.setStatus("C"); // C = Cancelada
            quoteRepository.save(quote);
        });
    }

    @Override
    public List<QuoteResponse> findByUserId(Long userId) {
        return quoteRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<QuoteResponse> findByDate(LocalDate date) {
        return quoteRepository.findByQuoteDate(date).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isTimeSlotAvailable(Long roomId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        List<Quote> conflicts = quoteRepository.findConflictingQuotes(roomId, date, startTime, endTime);
        return conflicts.isEmpty();
    }

    private QuoteResponse toResponse(Quote quote) {
        return QuoteResponse.builder()
                .id(quote.getId())
                .quoteDate(quote.getQuoteDate())
                .startTime(quote.getStartTime())
                .endTime(quote.getEndTime())
                .status(quote.getStatus())
                .userId(quote.getUser().getId())
                .userName(quote.getUser().getName() + " " + (quote.getUser().getLastname() != null ? quote.getUser().getLastname() : ""))
                .serviceId(quote.getService().getId())
                .serviceName(quote.getService().getName())
                .roomId(quote.getRoom().getId())
                .roomName(quote.getRoom().getName())
                .build();
    }
}
