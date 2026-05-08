package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto;

// Used by Grafana to show historical status of each service

import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.model.ServiceHealthStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "A single health-check result entry from the check history of a monitored service")
public record ServiceStatusHistoryDto(

        @Schema(description = "Unique identifier of this check result record", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,

        @Schema(description = "Health status recorded for this check", example = "UP")
        ServiceHealthStatus status,

        @Schema(description = "HTTP status code returned by the service for this check", example = "200")
        int httpStatusCode,

        @Schema(description = "Round-trip response time for this check in milliseconds", example = "87")
        long responseTimeMs,

        @Schema(description = "Timestamp when this health check was performed")
        LocalDateTime checkedAt
) {}
