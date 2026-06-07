package com.medtate.CalBag.calendar.repository;

import com.medtate.CalBag.calendar.domain.Calendar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CalendarRepository extends JpaRepository<Calendar, Integer> {

    @Query("SELECT c FROM Calendar c JOIN c.members m JOIN FETCH c.owner WHERE m.user.id = :userId")
    List<Calendar> findAllByUserId(@Param("userId") Integer userId);
}