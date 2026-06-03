package com.medtate.CalBag.calendar.repository;

import com.medtate.CalBag.calendar.domain.CalendarMember;
import com.medtate.CalBag.calendar.domain.CalendarRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CalendarMemberRepository extends JpaRepository<CalendarMember, Integer> {

    Optional<CalendarMember> findByCalendarIdAndUserId(Integer calendarId, Integer userId);
    boolean existsByCalendarIdAndUserId(Integer calendarId, Integer userId);
}