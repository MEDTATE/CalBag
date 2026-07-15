package com.medtate.CalBag.event.domain;

import com.medtate.CalBag.calendar.domain.Calendar;
import com.medtate.CalBag.user.domain.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "events",
        indexes = @Index(
                name = "idx_events_calendar_date",
                columnList = "calendar_id, start_at, end_at"
        )
)@Getter
@NoArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calendar_id", nullable = false)
    private Calendar calendar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(nullable = false)
    private String title;

    private String description;

    private String color;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime endAt;

    @Column(nullable = false)
    private boolean isAllDay;

    @Version
    private Integer version;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Event(Calendar calendar, User createdBy, String title, String description,
                 String color, LocalDateTime startAt, LocalDateTime endAt, boolean isAllDay) {
        this.calendar = calendar;
        this.createdBy = createdBy;
        this.title = title;
        this.description = description;
        this.color = color;
        this.startAt = startAt;
        this.endAt = endAt;
        this.isAllDay = isAllDay;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void update(String title, String description, String color,
                       LocalDateTime startAt, LocalDateTime endAt, boolean isAllDay) {
        this.title = title;
        this.description = description;
        this.color = color;
        this.startAt = startAt;
        this.endAt = endAt;
        this.isAllDay = isAllDay;
        this.updatedAt = LocalDateTime.now();
    }
}