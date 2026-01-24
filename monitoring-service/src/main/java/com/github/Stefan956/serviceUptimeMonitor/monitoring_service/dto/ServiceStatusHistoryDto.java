package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto;

// Used by Grafana to show historical status of each service

import java.time.LocalDateTime;

public record ServiceStatusHistoryDto(
        LocalDateTime timestamp,
        String status,  //UP or DOWN
        long responseTimeMs
) {}