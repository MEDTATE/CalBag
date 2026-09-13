package com.medtate.CalBag.notification.service;

import com.medtate.CalBag.calendar.repository.CalendarMemberRepository;
import com.medtate.CalBag.event.domain.Event;
import com.medtate.CalBag.event.repository.EventRepository;
import com.medtate.CalBag.global.exception.BusinessException;
import com.medtate.CalBag.notification.domain.Notification;
import com.medtate.CalBag.notification.domain.NotificationStatus;
import com.medtate.CalBag.notification.domain.NotificationType;
import com.medtate.CalBag.notification.dto.NotificationCreateRequest;
import com.medtate.CalBag.notification.dto.NotificationResponse;
import com.medtate.CalBag.notification.repository.NotificationRepository;
import com.medtate.CalBag.user.domain.User;
import com.medtate.CalBag.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CalendarMemberRepository calendarMemberRepository;

    @Transactional
    public NotificationResponse createNotification(Integer userId, Integer eventId,
                                                   NotificationCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("유저를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException("일정을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        if (!calendarMemberRepository.existsByCalendarIdAndUserId(event.getCalendar().getId(), userId)) {
            throw new BusinessException("접근 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        if (notificationRepository.existsByEventIdAndUserIdAndType(eventId, userId, request.getType())) {
            throw new BusinessException("같은 유형의 알림이 이미 설정되어 있습니다.", HttpStatus.CONFLICT);
        }

        LocalDateTime scheduledAt = calculateScheduledAt(event.getStartAt(), request.getType());

        if (scheduledAt.isBefore(LocalDateTime.now())) {
            throw new BusinessException("이미 지난 시각에는 알림을 설정할 수 없습니다.", HttpStatus.BAD_REQUEST);
        }

        Notification notification = Notification.builder()
                .user(user)
                .event(event)
                .type(request.getType())
                .scheduledAt(scheduledAt)
                .build();

        notificationRepository.save(notification);

        return new NotificationResponse(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(Integer userId) {
        return notificationRepository
                .findByUserIdAndStatusIn(userId, List.of(NotificationStatus.PENDING, NotificationStatus.SENT))
                .stream()
                .map(NotificationResponse::new)
                .toList();
    }

    @Transactional
    public void markAsRead(Integer userId, Integer notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException("알림을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        if (!notification.getUser().getId().equals(userId)) {
            throw new BusinessException("접근 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        notification.markAsRead();
        notificationRepository.save(notification);
    }

    private LocalDateTime calculateScheduledAt(LocalDateTime eventStartAt, NotificationType type) {
        return switch (type) {
            case TEN_MINUTES_BEFORE -> eventStartAt.minusMinutes(10);
            case THIRTY_MINUTES_BEFORE -> eventStartAt.minusMinutes(30);
            case ONE_HOUR_BEFORE -> eventStartAt.minusHours(1);
            case ONE_DAY_BEFORE -> eventStartAt.minusDays(1);
        };
    }
}