package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto;

// Shows the current state of each service
// Used by Grafana, Alert service
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.model.ServiceHealthStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ServiceStatusSummaryDto(
        UUID id,
        String serviceName,
        ServiceHealthStatus status,  //UP or DOWN
        int httpStatusCode,
        long responseTimeMs,
        LocalDateTime checkedAt
) {}