package com.spa.backend.repository;

import com.spa.backend.model.Quote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface QuoteRepository extends JpaRepository<Quote, Long> {
    List<Quote> findByUserId(Long userId);
    List<Quote> findByQuoteDate(LocalDate date);
    
    @Query("SELECT q FROM Quote q WHERE q.room.id = :roomId AND q.quoteDate = :date " +
           "AND q.status <> 'C' AND ((q.startTime < :endTime AND q.endTime > :startTime))")
    List<Quote> findConflictingQuotes(@Param("roomId") Long roomId, 
                                       @Param("date") LocalDate date,
                                       @Param("startTime") LocalTime startTime,
                                       @Param("endTime") LocalTime endTime);
}
