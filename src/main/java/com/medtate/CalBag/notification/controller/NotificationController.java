package com.medtate.CalBag.notification.controller;

import com.medtate.CalBag.global.response.ApiResponse;
import com.medtate.CalBag.notification.dto.NotificationCreateRequest;
import com.medtate.CalBag.notification.dto.NotificationResponse;
import com.medtate.CalBag.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/events/{eventId}/notification-settings")
    public ResponseEntity<ApiResponse<NotificationResponse>> createNotification(
            @AuthenticationPrincipal Long userId,
            @PathVariable Integer eventId,
            @Valid @RequestBody NotificationCreateRequest request) {
        NotificationResponse response = notificationService.createNotification(
                userId.intValue(), eventId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotifications(
            @AuthenticationPrincipal Long userId) {
        List<NotificationResponse> response = notificationService.getNotifications(userId.intValue());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping("/notifications/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @AuthenticationPrincipal Long userId,
            @PathVariable Integer notificationId) {
        notificationService.markAsRead(userId.intValue(), notificationId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}