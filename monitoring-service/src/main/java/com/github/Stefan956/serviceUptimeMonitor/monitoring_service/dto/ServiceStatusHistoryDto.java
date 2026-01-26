package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto;

// Used by Grafana to show historical status of each service

import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.model.ServiceHealthStatus;

import java.time.LocalDateTime;

public record ServiceStatusHistoryDto(
        LocalDateTime timestamp,
        ServiceHealthStatus status,  //UP or DOWN
        long responseTimeMs
) {}