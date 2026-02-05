package com.spa.backend.service;

import com.spa.backend.model.WorkSchedule;
import java.util.List;

public interface WorkScheduleService {
    List<WorkSchedule> getAll();
    WorkSchedule getByDayOfWeek(String dayOfWeek);
    WorkSchedule save(WorkSchedule schedule);
    void delete(Long id);
    WorkSchedule getById(Long id);
}
