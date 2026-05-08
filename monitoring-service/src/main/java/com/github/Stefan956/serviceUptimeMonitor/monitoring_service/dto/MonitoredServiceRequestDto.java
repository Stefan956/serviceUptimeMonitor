package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

@Schema(description = "Request body for registering or updating a monitored service")
public record MonitoredServiceRequestDto(

        @Schema(
                description = "Human-readable name that identifies the service",
                example = "Payment Service",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Service name is required")
        String name,

        @Schema(
                description = "Fully-qualified URL the scheduler will HTTP GET to check health",
                example = "https://api.example.com/health",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Service URL is required")
        @URL(message = "URL must be valid")
        String url,

        @Schema(
                description = "How often (in seconds) to run the health check. Minimum value is 5.",
                example = "30",
                minimum = "5",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @Min(value = 5, message = "Check interval must be at least 5 seconds")
        @Max(value = 86400, message = "Check interval must be at most 86400 seconds (1 day)")
        int checkIntervalSeconds
) {}
