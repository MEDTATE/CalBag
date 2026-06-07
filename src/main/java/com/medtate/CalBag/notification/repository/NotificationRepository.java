package com.medtate.CalBag.notification.repository;

import com.medtate.CalBag.notification.domain.Notification;
import com.medtate.CalBag.notification.domain.NotificationStatus;
import com.medtate.CalBag.notification.domain.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    List<Notification> findByUserIdAndStatusIn(Integer userId, List<NotificationStatus> statuses);

    boolean existsByEventIdAndUserIdAndType(Integer eventId, Integer userId, NotificationType type);
}