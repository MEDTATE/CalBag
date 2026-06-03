package com.medtate.CalBag.event.dto;

import com.medtate.CalBag.event.domain.Event;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class EventResponse {

    private final Integer id;
    private final String title;
    private final String description;
    private final String color;
    private final LocalDateTime startAt;
    private final LocalDateTime endAt;
    private final boolean isAllDay;
    private final Integer version;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public EventResponse(Event event) {
        this.id = event.getId();
        this.title = event.getTitle();
        this.description = event.getDescription();
        this.color = event.getColor();
        this.startAt = event.getStartAt();
        this.endAt = event.getEndAt();
        this.isAllDay = event.isAllDay();
        this.version = event.getVersion();
        this.createdAt = event.getCreatedAt();
        this.updatedAt = event.getUpdatedAt();
    }
}