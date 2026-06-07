package com.medtate.CalBag.notification.scheduler;

import com.medtate.CalBag.notification.domain.Notification;
import com.medtate.CalBag.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final RedisTemplate<String, String> redisTemplate;
    private final NotificationRepository notificationRepository;

    private static final String NOTIFICATION_KEY = "notification:schedule";

    public void scheduleNotification(Integer notificationId, Instant scheduledAt) {
        redisTemplate.opsForZSet().add(
                NOTIFICATION_KEY,
                String.valueOf(notificationId),
                scheduledAt.toEpochMilli()
        );
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void processScheduledNotifications() {
        double now = Instant.now().toEpochMilli();

        Set<String> dueNotifications = redisTemplate.opsForZSet()
                .rangeByScore(NOTIFICATION_KEY, 0, now);

        if (dueNotifications == null || dueNotifications.isEmpty()) {
            return;
        }

        for (String notificationIdStr : dueNotifications) {
            try {
                Integer notificationId = Integer.parseInt(notificationIdStr);
                Notification notification = notificationRepository.findById(notificationId)
                        .orElse(null);

                if (notification != null) {
                    notification.markAsSent();
                    notificationRepository.save(notification);
                    log.info("알림 발송 완료: eventId={}, userId={}",
                            notification.getEvent().getId(),
                            notification.getUser().getId());
                }

                redisTemplate.opsForZSet().remove(NOTIFICATION_KEY, notificationIdStr);
            } catch (Exception e) {
                log.error("알림 처리 실패: notificationId={}", notificationIdStr, e);
            }
        }
    }
}