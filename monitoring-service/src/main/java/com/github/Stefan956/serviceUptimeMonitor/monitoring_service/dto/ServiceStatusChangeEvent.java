package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto;

import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.model.ServiceHealthStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ServiceStatusChangeEvent(
        UUID serviceId,
        String serviceName,
        ServiceHealthStatus oldStatus,
        ServiceHealthStatus newStatus,
        int httpStatusCode,
        LocalDateTime changedAt
) {}