package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto;

import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.model.ServiceHealthStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ServiceStatusChangeDto(
        UUID id,
        String serviceName,
        ServiceHealthStatus oldStatus,
        ServiceHealthStatus newStatus,
        LocalDateTime changedAt
) {}

// Used by Alert Service to notify about status changes