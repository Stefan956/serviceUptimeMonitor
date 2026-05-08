package com.github.Stefan956.serviceUptimeMonitor.dashboard_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Metadata for a registered monitored service (as seen by the Dashboard Service)")
public record MonitoredServiceDto(

        @Schema(description = "Unique identifier of the service", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,

        @Schema(description = "Human-readable name of the service", example = "Payment Service")
        String name,

        @Schema(description = "URL that is health-checked on each scheduler tick", example = "https://api.example.com/health")
        String url,

        @Schema(description = "Frequency of health checks in seconds", example = "30")
        int checkIntervalSeconds,

        @Schema(description = "Whether the service is currently included in scheduled checks", example = "true")
        boolean enabled,

        @Schema(description = "Timestamp when the service was first registered")
        LocalDateTime createdAt,

        @Schema(description = "Timestamp of the most recent update to the service configuration")
        LocalDateTime updatedAt
) {}
