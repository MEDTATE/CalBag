package com.medtate.CalBag.notification.dto;

import com.medtate.CalBag.notification.domain.NotificationType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class NotificationCreateRequest {

    @NotNull(message = "알림 유형은 필수입니다.")
    private NotificationType type;
}