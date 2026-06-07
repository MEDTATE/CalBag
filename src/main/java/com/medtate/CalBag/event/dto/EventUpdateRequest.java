package com.medtate.CalBag.event.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EventUpdateRequest {

    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    private String description;

    private String color;

    @NotNull(message = "시작 시각은 필수입니다.")
    private LocalDateTime startAt;

    @NotNull(message = "종료 시각은 필수입니다.")
    private LocalDateTime endAt;

    private boolean isAllDay;

    @NotNull(message = "version은 필수입니다.")
    private Integer version;
}