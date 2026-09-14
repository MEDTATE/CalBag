package com.medtate.CalBag.calendar.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter 
public class JoinCalendarRequest {
    @NotBlank (message = "초대 코드는 필수입니다.")
    private String code;
}
