package com.github.Stefan956.serviceUptimeMonitor.alert_service.dto;

import com.github.Stefan956.serviceUptimeMonitor.alert_service.model.NotificationChannel;
import com.github.Stefan956.serviceUptimeMonitor.alert_service.model.ServiceHealthStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Persisted alert record returned by query endpoints")
public record AlertResponseDto(

        @Schema(description = "Unique identifier of the alert record", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,

        @Schema(description = "Name of the monitored service that triggered this alert", example = "Payment Service")
        String serviceName,

        @Schema(description = "Health status before the transition", example = "UP")
        ServiceHealthStatus oldStatus,

        @Schema(description = "Health status after the transition", example = "DOWN")
        ServiceHealthStatus newStatus,

        @Schema(description = "HTTP status code at the time the status changed", example = "503")
        int httpStatusCode,

        @Schema(description = "Timestamp when the Monitoring Service detected the status change")
        LocalDateTime changedAt,

        @Schema(description = "Timestamp when the Alert Service dispatched the notification")
        LocalDateTime notifiedAt,

        @Schema(description = "Notification channel used to deliver this alert (EMAIL, SLACK, WEBHOOK)", example = "EMAIL")
        NotificationChannel notificationChannel
) {}
