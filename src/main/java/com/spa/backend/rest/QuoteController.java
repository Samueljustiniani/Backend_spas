package com.spa.backend.rest;

import com.spa.backend.dto.QuoteRequest;
import com.spa.backend.dto.QuoteResponse;
import com.spa.backend.service.QuoteService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/v1/api/quotes")
public class QuoteController {
        @PatchMapping("/{id}/reactivate")
        @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
        public ResponseEntity<QuoteResponse> reactivate(@PathVariable Long id) {
            QuoteResponse updated = quoteService.reactivateQuote(id);
            if (updated == null) return ResponseEntity.badRequest().build();
            return ResponseEntity.ok(updated);
        }
    @GetMapping("/hours-range")
    public ResponseEntity<?> getHoursRange() {
        // Puedes cambiar estos valores si lo deseas
        LocalTime opening = LocalTime.of(8, 0);
        LocalTime closing = LocalTime.of(20, 0);
        return ResponseEntity.ok(new java.util.HashMap<String, String>() {{
            put("opening", opening.toString());
            put("closing", closing.toString());
        }});
    }

    private final QuoteService quoteService;

    public QuoteController(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<QuoteResponse>> list() {
        return ResponseEntity.ok(quoteService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<QuoteResponse> get(@PathVariable Long id) {
        return quoteService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER') and (@userService.isOwner(#userId, principal.username) or hasRole('ADMIN'))")
    public ResponseEntity<List<QuoteResponse>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(quoteService.findByUserId(userId));
    }

    @GetMapping("/date/{date}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<QuoteResponse>> getByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(quoteService.findByDate(date));
    }

    @GetMapping("/availability")
    public ResponseEntity<Boolean> checkAvailability(
            @RequestParam Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime) {
        return ResponseEntity.ok(quoteService.isTimeSlotAvailable(roomId, date, startTime, endTime));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<QuoteResponse> create(@RequestBody QuoteRequest request) {
        QuoteResponse saved = quoteService.create(request);
        return ResponseEntity.created(URI.create("/v1/api/quotes/" + saved.getId())).body(saved);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<QuoteResponse> updateStatus(@PathVariable Long id, @RequestParam String status) {
        QuoteResponse updated = quoteService.updateStatus(id, status);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (quoteService.findById(id).isEmpty()) return ResponseEntity.notFound().build();
        quoteService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
