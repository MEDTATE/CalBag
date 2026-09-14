package com.medtate.CalBag.calendar.dto;

import java.time.LocalDateTime;

import com.medtate.CalBag.calendar.domain.Calendar;

import lombok.Getter;

@Getter 
public class InviteCodeResponse {
    private final String code;
    private final LocalDateTime expiresAt;

    public InviteCodeResponse(Calendar calendar) {
        this.code = calendar.getInviteCode();
        this.expiresAt = calendar.getInviteCodeExpiresAt();
    }
}
