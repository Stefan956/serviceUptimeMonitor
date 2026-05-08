package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto;

// Used by Grafana to show historical status of each service

import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.model.ServiceHealthStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "The most recent health-check result for a monitored service")
public record ServiceStatusSummaryDto(

        @Schema(description = "Unique identifier of the monitored service", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,

        @Schema(description = "Human-readable name of the service", example = "Payment Service")
        String serviceName,

        @Schema(description = "Current health status of the service", example = "UP")
        ServiceHealthStatus status,

        @Schema(description = "HTTP status code returned by the last health check", example = "200")
        int httpStatusCode,

        @Schema(description = "Round-trip response time of the last health check in milliseconds", example = "142")
        long responseTimeMs,

        @Schema(description = "Timestamp when the last health check was performed")
        LocalDateTime checkedAt
) {}
