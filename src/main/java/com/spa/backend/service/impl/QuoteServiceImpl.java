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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class QuoteServiceImpl implements QuoteService {

    @Value("${app.pricing.price-per-hour:100.00}")
    private BigDecimal pricePerHour;

    @Value("${app.pricing.max-duration-hours:4}")
    private int maxDurationHours;

    @Override
    public List<LocalTime> getAvailableSlots(Long roomId, LocalDate date, List<Long> serviceIds) {
        // Calcular duración total de los servicios seleccionados
        int totalMinutes = 0;
        if (serviceIds != null && !serviceIds.isEmpty()) {
            for (Long serviceId : serviceIds) {
                ServiceEntity service = serviceRepository.findById(serviceId).orElse(null);
                if (service != null && service.getDurationMinutes() != null) {
                    totalMinutes += service.getDurationMinutes();
                } else {
                    totalMinutes += 60; // Default 1 hora si no tiene duración definida
                }
            }
        } else {
            totalMinutes = 60; // Default 1 hora
        }

        // Horario de atención
        LocalTime opening = LocalTime.of(8, 0);
        LocalTime closing = LocalTime.of(20, 0);

        List<LocalTime> availableSlots = new ArrayList<>();

        // Generar slots cada 1 hora (8:00, 9:00, 10:00, etc.)
        for (LocalTime slotStart = opening; !slotStart.plusMinutes(totalMinutes).isAfter(closing); slotStart = slotStart.plusHours(1)) {
            LocalTime slotEnd = slotStart.plusMinutes(totalMinutes);
            
            // Si es hoy, no mostrar horas pasadas
            if (date.equals(LocalDate.now()) && slotStart.isBefore(LocalTime.now())) {
                continue;
            }

            // Verificar si el slot está disponible
            if (isTimeSlotAvailable(roomId, date, slotStart, slotEnd)) {
                availableSlots.add(slotStart);
            }
        }

        return availableSlots;
    }

    @Override
    public java.util.Map<String, Object> calculateEstimate(List<Long> serviceIds) {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        
        int totalMinutes = 0;
        List<String> serviceNames = new ArrayList<>();
        
        if (serviceIds != null && !serviceIds.isEmpty()) {
            for (Long serviceId : serviceIds) {
                ServiceEntity service = serviceRepository.findById(serviceId).orElse(null);
                if (service != null) {
                    totalMinutes += service.getDurationMinutes() != null ? service.getDurationMinutes() : 60;
                    serviceNames.add(service.getName());
                }
            }
        }

        // Calcular precio: redondear hacia arriba a la hora completa más cercana
        int hoursRoundedUp = (int) Math.ceil(totalMinutes / 60.0);
        BigDecimal totalPrice = pricePerHour.multiply(BigDecimal.valueOf(hoursRoundedUp));

        result.put("totalDurationMinutes", totalMinutes);
        result.put("hoursRoundedUp", hoursRoundedUp);
        result.put("totalPrice", totalPrice);
        result.put("pricePerHour", pricePerHour);
        result.put("serviceNames", serviceNames);
        result.put("maxDurationHours", maxDurationHours);
        result.put("exceedsMaxDuration", totalMinutes > (maxDurationHours * 60));

        return result;
    }

    @Override
    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 600000) // Cada 10 minutos
    public void markPendingQuotesInactive() {
        // Cancelar citas pendientes creadas hace más de 2 horas sin confirmar
        java.time.LocalDateTime twoHoursAgo = java.time.LocalDateTime.now().minusHours(2);
        List<Quote> allPending = quoteRepository.findAll().stream()
            .filter(q -> "P".equals(q.getStatus()))
            .filter(q -> q.getCreatedAt() != null && q.getCreatedAt().isBefore(twoHoursAgo))
            .collect(Collectors.toList());
        
        for (Quote q : allPending) {
            q.setStatus("I"); // I = Inactiva (no confirmada en 2 horas, horario liberado)
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
        if (request.getStartTime() == null) throw new RuntimeException("La hora de inicio no puede ser null");

        // Obtener servicios y calcular duración total
        List<ServiceEntity> services = request.getServiceIds().stream()
            .map(id -> {
                if (id == null) throw new RuntimeException("Uno de los ids de servicio es null");
                return serviceRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Servicio no encontrado: " + id));
            })
            .collect(Collectors.toList());

        // Calcular duración total en minutos sumando la duración de cada servicio
        int totalDurationMinutes = services.stream()
            .mapToInt(s -> s.getDurationMinutes() != null ? s.getDurationMinutes() : 60)
            .sum();

        // Validar que la duración no exceda el máximo permitido (default 4 horas = 240 minutos)
        int maxMinutes = maxDurationHours * 60;
        if (totalDurationMinutes > maxMinutes) {
            throw new RuntimeException("La duración total de los servicios (" + totalDurationMinutes + " min) excede el máximo permitido de " + maxDurationHours + " horas");
        }

        // Calcular endTime automáticamente
        LocalTime endTime = request.getStartTime().plusMinutes(totalDurationMinutes);

        // Validar rango horario permitido (08:00 a 20:00)
        LocalTime opening = LocalTime.of(8, 0);
        LocalTime closing = LocalTime.of(20, 0);
        if (request.getStartTime().isBefore(opening) || endTime.isAfter(closing)) {
            throw new RuntimeException("Las citas solo pueden agendarse entre 08:00 y 20:00. La cita terminaría a las " + endTime);
        }

        // Validar que no se creen citas en fechas pasadas
        LocalDate today = LocalDate.now();
        if (request.getQuoteDate().isBefore(today)) {
            throw new RuntimeException("No se pueden crear citas en fechas pasadas");
        }

        // Validar que si es el mismo día, la hora de inicio sea posterior a la hora actual
        if (request.getQuoteDate().isEqual(today)) {
            LocalTime now = LocalTime.now();
            if (request.getStartTime().isBefore(now)) {
                throw new RuntimeException("No se pueden crear citas en horas que ya pasaron");
            }
        }

        // Validar disponibilidad del horario
        if (!isTimeSlotAvailable(request.getRoomId(), request.getQuoteDate(), 
            request.getStartTime(), endTime)) {
            throw new RuntimeException("El horario seleccionado no está disponible");
        }

        // Calcular precio: redondear hacia arriba a la hora completa más cercana
        int hoursRoundedUp = (int) Math.ceil(totalDurationMinutes / 60.0);
        BigDecimal totalPrice = pricePerHour.multiply(BigDecimal.valueOf(hoursRoundedUp));

        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Room room = roomRepository.findById(request.getRoomId())
            .orElseThrow(() -> new RuntimeException("Sala no encontrada"));

        Quote quote = new Quote();
        quote.setUser(user);
        quote.setServices(services);
        quote.setRoom(room);
        quote.setQuoteDate(request.getQuoteDate());
        quote.setStartTime(request.getStartTime());
        quote.setEndTime(endTime);
        quote.setTotalDurationMinutes(totalDurationMinutes);
        quote.setTotalPrice(totalPrice);
        quote.setStatus("P"); // P = Pendiente
        quote.setCreatedAt(java.time.LocalDateTime.now());

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

    @Override
    public boolean canUserAccessQuotes(Long userId, String email) {
        // Verificar si el usuario es el dueño de las citas o es admin
        return userRepository.findById(userId)
            .map(user -> {
                // Si el email coincide, puede ver sus citas
                if (user.getEmail().equalsIgnoreCase(email)) {
                    return true;
                }
                // Si el usuario autenticado es admin, puede ver cualquier cita
                return userRepository.findByEmail(email)
                    .map(authUser -> "ROLE_ADMIN".equals(authUser.getRole()))
                    .orElse(false);
            })
            .orElse(false);
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
            .totalDurationMinutes(quote.getTotalDurationMinutes())
            .totalPrice(quote.getTotalPrice())
            .build();
    }
}
