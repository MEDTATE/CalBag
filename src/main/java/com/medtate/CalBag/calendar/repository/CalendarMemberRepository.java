package com.medtate.CalBag.calendar.repository;

import com.medtate.CalBag.calendar.domain.CalendarMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CalendarMemberRepository extends JpaRepository<CalendarMember, Integer> {

    @Query("SELECT m FROM CalendarMember m JOIN FETCH m.user WHERE m.calendar.id = :calendarId ORDER BY m.joinedAt")
    List<CalendarMember> findAllWithUserByCalendarId(@Param("calendarId") Integer calendarId);

    Optional<CalendarMember> findByCalendarIdAndUserId(Integer calendarId, Integer userId);

    boolean existsByCalendarIdAndUserId(Integer calendarId, Integer userId);
}