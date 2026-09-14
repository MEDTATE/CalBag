package com.medtate.CalBag.calendar.dto;

import java.time.LocalDateTime;

import com.medtate.CalBag.calendar.domain.CalendarMember;
import com.medtate.CalBag.calendar.domain.CalendarRole;

import lombok.Getter;

@Getter 
public class MemberResponse {
    private final Integer userId;
    private final String name;
    private final CalendarRole role;
    private final LocalDateTime joinedAt;

    public MemberResponse(CalendarMember member) {
        this.userId = member.getUser().getId();
        this.name = member.getUser().getName();
        this.role = member.getRole();
        this.joinedAt = member.getJoinedAt();
    }
}
