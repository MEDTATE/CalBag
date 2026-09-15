package com.medtate.CalBag.notification.repository;

import com.medtate.CalBag.notification.domain.Notification;
import com.medtate.CalBag.notification.domain.NotificationStatus;
import com.medtate.CalBag.notification.domain.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    @Query("SELECT n FROM Notification n JOIN FETCH n.event WHERE n.user.id = :userId AND n.status IN :statuses")
    List<Notification> findByUserIdAndStatusIn(@Param("userId") Integer userId,
                                               @Param("statuses") List<NotificationStatus> statuses);

    List<Notification> findByStatusAndScheduledAtLessThanEqual(NotificationStatus status, LocalDateTime now);

    boolean existsByEventIdAndUserIdAndType(Integer eventId, Integer userId, NotificationType type);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.user.id = :userId " +
            "AND n.event.id IN (SELECT e.id FROM Event e WHERE e.calendar.id = :calendarId)")
    void deleteByUserIdAndCalendarId(@Param("userId") Integer userId,
                                     @Param("calendarId") Integer calendarId);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.event.id IN (SELECT e.id FROM Event e WHERE e.calendar.id = :calendarId)")
    void deleteByCalendarId(@Param("calendarId") Integer calendarId);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.event.id = :eventId")
    void deleteByEventId(@Param("eventId") Integer eventId);

}