package com.github.Stefan956.serviceUptimeMonitor.alert_service.dto;

import com.github.Stefan956.serviceUptimeMonitor.alert_service.model.ServiceHealthStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Status-change event payload sent by the Monitoring Service")
public record AlertRequestDto(

        @Schema(
                description = "UUID of the monitored service that changed state",
                example = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull UUID serviceId,

        @Schema(
                description = "Human-readable name of the service that changed state",
                example = "Payment Service",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank String serviceName,

        @Schema(
                description = "Health status of the service before the transition",
                example = "UP",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull ServiceHealthStatus oldStatus,

        @Schema(
                description = "Health status of the service after the transition",
                example = "DOWN",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull ServiceHealthStatus newStatus,

        @Schema(
                description = "HTTP status code returned by the service at the moment of the failing check",
                example = "503"
        )
        int httpStatusCode,

        @Schema(
                description = "Timestamp when the status change was detected by the Monitoring Service",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull LocalDateTime changedAt
) {}
