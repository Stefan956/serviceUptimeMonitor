package com.github.Stefan956.serviceUptimeMonitor.dashboard_service.dto;

import com.github.Stefan956.serviceUptimeMonitor.dashboard_service.model.ServiceHealthStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "A single health-check history entry for a monitored service (as seen by the Dashboard Service)")
public record ServiceStatusHistoryDto(

        @Schema(description = "Unique identifier of this check result record", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,

        @Schema(description = "Health status recorded for this check", example = "DOWN")
        ServiceHealthStatus status,

        @Schema(description = "HTTP status code returned by the service for this check", example = "503")
        int httpStatusCode,

        @Schema(description = "Round-trip response time for this check in milliseconds", example = "87")
        long responseTimeMs,

        @Schema(description = "Timestamp when this health check was performed")
        LocalDateTime checkedAt
) {}
