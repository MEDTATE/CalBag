package com.medtate.CalBag.notification.dto;

import com.medtate.CalBag.notification.domain.Notification;
import com.medtate.CalBag.notification.domain.NotificationStatus;
import com.medtate.CalBag.notification.domain.NotificationType;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NotificationResponse {

    private final Integer id;
    private final Integer eventId;
    private final String eventTitle;
    private final NotificationType type;
    private final NotificationStatus status;
    private final LocalDateTime scheduledAt;
    private final LocalDateTime sentAt;

    public NotificationResponse(Notification notification) {
        this.id = notification.getId();
        this.eventId = notification.getEvent().getId();
        this.eventTitle = notification.getEvent().getTitle();
        this.type = notification.getType();
        this.status = notification.getStatus();
        this.scheduledAt = notification.getScheduledAt();
        this.sentAt = notification.getSentAt();
    }
}