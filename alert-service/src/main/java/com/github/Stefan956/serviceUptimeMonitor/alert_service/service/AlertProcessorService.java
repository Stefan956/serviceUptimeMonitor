package com.github.Stefan956.serviceUptimeMonitor.alert_service.service;

import com.github.Stefan956.serviceUptimeMonitor.alert_service.dao.AlertRepository;
import com.github.Stefan956.serviceUptimeMonitor.alert_service.dto.AlertRequestDto;
import com.github.Stefan956.serviceUptimeMonitor.alert_service.dto.AlertResponseDto;
import com.github.Stefan956.serviceUptimeMonitor.alert_service.exception.AlertProcessingException;
import com.github.Stefan956.serviceUptimeMonitor.alert_service.model.Alert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertProcessorService {

    private final AlertRepository alertRepository;
    private final List<NotificationService> notificationServices;

    @Value("${alert.cooldown-ms}")
    private long cooldownMs;

    @Transactional
    public void processStatusChange(AlertRequestDto request) {
        if (isInCooldown(request)) {
            log.info("Alert for service '{}' with status {} is within cooldown period, skipping",
                    request.serviceName(), request.newStatus());
            return;
        }

        if (notificationServices == null || notificationServices.isEmpty()) {
            log.warn("No notification services configured, skipping alert for service: {}", request.serviceName());
            return;
        }

        for (NotificationService notificationService : notificationServices) {
            Alert alert = new Alert();
            alert.setServiceName(request.serviceName());
            alert.setOldStatus(request.oldStatus());
            alert.setNewStatus(request.newStatus());
            alert.setHttpStatusCode(request.httpStatusCode());
            alert.setChangedAt(request.changedAt());
            alert.setNotificationChannel(notificationService.getChannel());

            try {
                alert = alertRepository.save(alert);
            } catch (Exception e) {
                throw new AlertProcessingException(
                        "Failed to persist alert for service: " + request.serviceName(), e);
            }
            notificationService.notify(alert);
        }
    }

    @Transactional(readOnly = true)
    public List<AlertResponseDto> getAllAlerts() {
        return alertRepository.findAllByOrderByNotifiedAtDesc()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AlertResponseDto> getAlertsByServiceName(String serviceName) {
        return alertRepository.findByServiceNameOrderByNotifiedAtDesc(serviceName)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private boolean isInCooldown(AlertRequestDto request) {
        Optional<Alert> lastAlert = alertRepository
                .findTopByServiceNameAndNewStatusOrderByNotifiedAtDesc(
                        request.serviceName(), request.newStatus());

        if (lastAlert.isEmpty()) {
            return false;
        }

        Duration elapsed = Duration.between(lastAlert.get().getNotifiedAt(), LocalDateTime.now());
        return elapsed.toMillis() < cooldownMs;
    }

    private AlertResponseDto toDto(Alert alert) {
        return new AlertResponseDto(
                alert.getId(),
                alert.getServiceName(),
                alert.getOldStatus(),
                alert.getNewStatus(),
                alert.getHttpStatusCode(),
                alert.getChangedAt(),
                alert.getNotifiedAt(),
                alert.getNotificationChannel()
        );
    }
}
