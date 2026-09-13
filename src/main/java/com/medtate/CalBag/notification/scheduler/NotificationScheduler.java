package com.medtate.CalBag.notification.scheduler;

import com.medtate.CalBag.notification.domain.Notification;
import com.medtate.CalBag.notification.domain.NotificationStatus;
import com.medtate.CalBag.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationRepository notificationRepository;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void processScheduledNotifications() {
        List<Notification> dueNotifications = notificationRepository
                .findByStatusAndScheduledAtLessThanEqual(NotificationStatus.PENDING, LocalDateTime.now());

        for (Notification notification : dueNotifications) {
            notification.markAsSent();
            log.info("알림 발송 완료: eventId={}, userId={}",
                    notification.getEvent().getId(),
                    notification.getUser().getId());
        }
    }
}