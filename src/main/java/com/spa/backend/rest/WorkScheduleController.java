package com.spa.backend.rest;

import com.spa.backend.model.WorkSchedule;
import com.spa.backend.service.WorkScheduleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/api/work-schedule")
public class WorkScheduleController {
            @PatchMapping("/{id}/inactivate")
            @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
            public ResponseEntity<WorkSchedule> inactivate(@PathVariable Long id) {
                WorkSchedule ws = service.getById(id);
                if (ws == null) return ResponseEntity.notFound().build();
                ws.setStatus("I");
                return ResponseEntity.ok(service.save(ws));
            }

            @PatchMapping("/{id}/activate")
            @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
            public ResponseEntity<WorkSchedule> activate(@PathVariable Long id) {
                WorkSchedule ws = service.getById(id);
                if (ws == null) return ResponseEntity.notFound().build();
                ws.setStatus("A");
                return ResponseEntity.ok(service.save(ws));
            }
        @GetMapping("/active")
        public ResponseEntity<List<WorkSchedule>> listActive() {
            return ResponseEntity.ok(service.getAll().stream()
                .filter(ws -> "A".equals(ws.getStatus()))
                .toList());
        }

        @GetMapping("/inactive")
        public ResponseEntity<List<WorkSchedule>> listInactive() {
            return ResponseEntity.ok(service.getAll().stream()
                .filter(ws -> "I".equals(ws.getStatus()))
                .toList());
        }
    private final WorkScheduleService service;

    public WorkScheduleController(WorkScheduleService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<WorkSchedule>> list() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{dayOfWeek}")
    public ResponseEntity<WorkSchedule> getByDay(@PathVariable String dayOfWeek) {
        WorkSchedule ws = service.getByDayOfWeek(dayOfWeek);
        if (ws == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ws);
    }

    @PostMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WorkSchedule> create(@RequestBody WorkSchedule ws) {
        return ResponseEntity.ok(service.save(ws));
    }

    @PutMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WorkSchedule> update(@PathVariable Long id, @RequestBody WorkSchedule ws) {
        ws.setId(id);
        return ResponseEntity.ok(service.save(ws));
    }

    @DeleteMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
