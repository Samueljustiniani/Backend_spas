package com.spa.backend.service.impl;

import com.spa.backend.model.WorkSchedule;
import com.spa.backend.repository.WorkScheduleRepository;
import com.spa.backend.service.WorkScheduleService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkScheduleServiceImpl implements WorkScheduleService {
    private final WorkScheduleRepository repo;

    public WorkScheduleServiceImpl(WorkScheduleRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<WorkSchedule> getAll() {
        return repo.findAll();
    }

    @Override
    public WorkSchedule getByDayOfWeek(String dayOfWeek) {
        List<WorkSchedule> list = repo.findByDayOfWeek(dayOfWeek);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public WorkSchedule save(WorkSchedule schedule) {
        return repo.save(schedule);
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }

    @Override
    public WorkSchedule getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("WorkSchedule not found with id: " + id));
    }
}
