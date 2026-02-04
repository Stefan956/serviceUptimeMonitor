package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto;

import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.model.ServiceHealthStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ServiceStatusChangeEvent(
        UUID id,
        String serviceName,
        ServiceHealthStatus oldStatus,
        ServiceHealthStatus newStatus,
        int httpStatusCode,
        LocalDateTime changedAt
) {}

// Used by Alert Service to notify about status changes