package com.medtate.CalBag.calendar.dto;

import com.medtate.CalBag.calendar.domain.Calendar;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CalendarResponse {

    private final Integer id;
    private final String title;
    private final String description;
    private final String color;
    private final LocalDateTime createdAt;

    public CalendarResponse(Calendar calendar) {
        this.id = calendar.getId();
        this.title = calendar.getTitle();
        this.description = calendar.getDescription();
        this.color = calendar.getColor();
        this.createdAt = calendar.getCreatedAt();
    }
}