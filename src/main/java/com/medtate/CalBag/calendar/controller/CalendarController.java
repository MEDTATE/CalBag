package com.medtate.CalBag.calendar.controller;

import com.medtate.CalBag.calendar.dto.CalendarCreateRequest;
import com.medtate.CalBag.calendar.dto.CalendarResponse;
import com.medtate.CalBag.calendar.dto.CalendarUpdateRequest;
import com.medtate.CalBag.calendar.dto.InviteCodeResponse;
import com.medtate.CalBag.calendar.dto.JoinCalendarRequest;
import com.medtate.CalBag.calendar.dto.MemberResponse;
import com.medtate.CalBag.calendar.service.CalendarService;
import com.medtate.CalBag.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/calendars")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    @PostMapping
    public ResponseEntity<ApiResponse<CalendarResponse>> createCalendar(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CalendarCreateRequest request) {
        CalendarResponse response = calendarService.createCalendar(userId.intValue(), request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CalendarResponse>>> getCalendars(
            @AuthenticationPrincipal Long userId) {
        List<CalendarResponse> response = calendarService.getCalendars(userId.intValue());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping("/{calendarId}")
    public ResponseEntity<ApiResponse<CalendarResponse>> updateCalendar(
            @AuthenticationPrincipal Long userId,
            @PathVariable Integer calendarId,
            @Valid @RequestBody CalendarUpdateRequest request) {
        CalendarResponse response = calendarService.updateCalendar(userId.intValue(), calendarId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{calendarId}")
    public ResponseEntity<ApiResponse<Void>> deleteCalendar(
            @AuthenticationPrincipal Long userId,
            @PathVariable Integer calendarId) {
        calendarService.deleteCalendar(userId.intValue(), calendarId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/{calendarId}/invite-code")
    public ResponseEntity<ApiResponse<InviteCodeResponse>> issueInviteCode(
            @AuthenticationPrincipal Long userId,
            @PathVariable Integer calendarId) {
        InviteCodeResponse response = calendarService.issueInviteCode(userId.intValue(), calendarId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/join")
    public ResponseEntity<ApiResponse<CalendarResponse>> joinCalendar(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody JoinCalendarRequest request) {
        CalendarResponse response = calendarService.joinByInviteCode(userId.intValue(), request.getCode());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{calendarId}/members")
    public ResponseEntity<ApiResponse<List<MemberResponse>>> getMembers(
            @AuthenticationPrincipal Long userId,
            @PathVariable Integer calendarId) {
        List<MemberResponse> response = calendarService.getMembers(userId.intValue(), calendarId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}