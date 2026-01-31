package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto;

// Used by Grafana to show historical status of each service

import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.model.ServiceHealthStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ServiceStatusHistoryDto(
        UUID id,
        ServiceHealthStatus status,//UP or DOWN
        int httpStatusCode,
        long responseTimeMs,
        LocalDateTime checkedAt
) {}