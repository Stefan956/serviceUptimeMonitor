package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record MonitoredServiceRequestDto (

    @NotBlank(message = "Service name is required")
    String name,

    @NotBlank(message = "Service URL is required")
    @URL(message = "URL must be valid")
    String url,

    @Min(value = 5, message = "Check interval must be at least 5 seconds")
    int checkIntervalSeconds
) {}
