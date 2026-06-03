package com.medtate.CalBag.event.repository;

import com.medtate.CalBag.event.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Integer> {

    @Query("SELECT e FROM Event e WHERE e.calendar.id = :calendarId " +
            "AND e.startAt <= :endAt AND e.endAt >= :startAt")
    List<Event> findByCalendarIdAndDateRange(
            @Param("calendarId") Integer calendarId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt);
}