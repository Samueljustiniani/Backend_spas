package com.spa.backend.repository;

import com.spa.backend.model.WorkSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, Long> {
    List<WorkSchedule> findByDayOfWeek(String dayOfWeek);
}
