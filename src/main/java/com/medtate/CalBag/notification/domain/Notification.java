package com.medtate.CalBag.notification.domain;

import com.medtate.CalBag.event.domain.Event;
import com.medtate.CalBag.user.domain.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table( name = "notifications",
        indexes = @Index(
            name = "idx_notifications_status_scheduled_at",
            columnList = "status, scheduled_at"
        )
)
@Getter
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    @Column(nullable = false)
    private LocalDateTime scheduledAt;

    private LocalDateTime sentAt;

    @Builder
    public Notification(User user, Event event, NotificationType type, LocalDateTime scheduledAt) {
        this.user = user;
        this.event = event;
        this.type = type;
        this.status = NotificationStatus.PENDING;
        this.scheduledAt = scheduledAt;
    }

    public void markAsSent() {
        this.status = NotificationStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }

    public void markAsFailed() {
        this.status = NotificationStatus.FAILED;
    }

    public void markAsRead() {
        this.status = NotificationStatus.READ;
    }
}