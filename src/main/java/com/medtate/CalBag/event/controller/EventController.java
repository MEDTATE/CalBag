package com.medtate.CalBag.event.controller;

import com.medtate.CalBag.event.dto.EventCreateRequest;
import com.medtate.CalBag.event.dto.EventResponse;
import com.medtate.CalBag.event.dto.EventUpdateRequest;
import com.medtate.CalBag.event.service.EventService;
import com.medtate.CalBag.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping("/calendars/{calendarId}/events")
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(
            @AuthenticationPrincipal Long userId,
            @PathVariable Integer calendarId,
            @Valid @RequestBody EventCreateRequest request) {
        EventResponse response = eventService.createEvent(userId.intValue(), calendarId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/calendars/{calendarId}/events")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getEvents(
            @AuthenticationPrincipal Long userId,
            @PathVariable Integer calendarId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startAt,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endAt) {
        List<EventResponse> response = eventService.getEvents(userId.intValue(), calendarId, startAt, endAt);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/events/{eventId}")
    public ResponseEntity<ApiResponse<EventResponse>> getEvent(
            @AuthenticationPrincipal Long userId,
            @PathVariable Integer eventId) {
        EventResponse response = eventService.getEvent(userId.intValue(), eventId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/events/{eventId}")
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(
            @AuthenticationPrincipal Long userId,
            @PathVariable Integer eventId,
            @Valid @RequestBody EventUpdateRequest request) {
        EventResponse response = eventService.updateEvent(userId.intValue(), eventId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/events/{eventId}")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(
            @AuthenticationPrincipal Long userId,
            @PathVariable Integer eventId) {
        eventService.deleteEvent(userId.intValue(), eventId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}