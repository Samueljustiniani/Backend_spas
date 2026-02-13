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
    @Override
    @org.springframework.scheduling.annotation.Scheduled(cron = "0 59 23 * * *") // Todos los días a las 23:59
    public void markPendingQuotesInactive() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<Quote> pendientes = quoteRepository.findByQuoteDateAndStatus(tomorrow, "P");
        for (Quote q : pendientes) {
            q.setStatus("I"); // I = Inactiva
            quoteRepository.save(q);
        }
    }

    @Override
    public QuoteResponse reactivateQuote(Long quoteId) {
        Optional<Quote> quoteOpt = quoteRepository.findById(quoteId);
        if (quoteOpt.isEmpty()) return null;
        Quote quote = quoteOpt.get();
        // Solo reactiva si el horario está libre
        boolean disponible = isTimeSlotAvailable(
            quote.getRoom().getId(),
            quote.getQuoteDate(),
            quote.getStartTime(),
            quote.getEndTime()
        );
        if (!disponible) return null;
        quote.setStatus("P");
        quoteRepository.save(quote);
        return toResponse(quote);
    }

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

        // Validar campos obligatorios
        if (request.getUserId() == null) throw new RuntimeException("El id de usuario no puede ser null");
        if (request.getRoomId() == null) throw new RuntimeException("El id de sala no puede ser null");
        if (request.getServiceIds() == null || request.getServiceIds().isEmpty()) throw new RuntimeException("Debe enviar al menos un id de servicio");
        if (request.getQuoteDate() == null) throw new RuntimeException("La fecha de la cita no puede ser null");
        if (request.getStartTime() == null || request.getEndTime() == null) throw new RuntimeException("La hora de inicio y fin no pueden ser null");

        // Validar rango horario permitido (08:00 a 20:00)
        LocalTime opening = LocalTime.of(8, 0);
        LocalTime closing = LocalTime.of(20, 0);
        if (request.getStartTime().isBefore(opening) || request.getEndTime().isAfter(closing)) {
            throw new RuntimeException("Las citas solo pueden agendarse entre 08:00 y 20:00");
        }

        // Validar disponibilidad del horario
        if (!isTimeSlotAvailable(request.getRoomId(), request.getQuoteDate(), 
            request.getStartTime(), request.getEndTime())) {
            throw new RuntimeException("El horario seleccionado no está disponible");
        }

        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        List<ServiceEntity> services = request.getServiceIds().stream()
            .map(id -> {
                if (id == null) throw new RuntimeException("Uno de los ids de servicio es null");
                return serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado: " + id));
            })
            .collect(Collectors.toList());
        Room room = roomRepository.findById(request.getRoomId())
            .orElseThrow(() -> new RuntimeException("Sala no encontrada"));

        Quote quote = new Quote();
        quote.setUser(user);
        quote.setServices(services);
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
            .serviceIds(quote.getServices().stream().map(ServiceEntity::getId).collect(Collectors.toList()))
            .serviceNames(quote.getServices().stream().map(ServiceEntity::getName).collect(Collectors.toList()))
            .roomId(quote.getRoom().getId())
            .roomName(quote.getRoom().getName())
            .build();
    }
}
