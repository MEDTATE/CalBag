package com.medtate.CalBag.calendar.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CalendarUpdateRequest {

    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    private String description;

    @NotBlank(message = "색상은 필수입니다.")
    private String color;
}