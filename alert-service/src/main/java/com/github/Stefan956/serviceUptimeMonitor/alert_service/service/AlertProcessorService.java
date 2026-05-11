package com.github.Stefan956.serviceUptimeMonitor.alert_service.service;

import com.github.Stefan956.serviceUptimeMonitor.alert_service.dao.AlertRepository;
import com.github.Stefan956.serviceUptimeMonitor.alert_service.dto.AlertRequestDto;
import com.github.Stefan956.serviceUptimeMonitor.alert_service.dto.AlertResponseDto;
import com.github.Stefan956.serviceUptimeMonitor.alert_service.exception.AlertProcessingException;
import com.github.Stefan956.serviceUptimeMonitor.alert_service.model.Alert;
import com.github.Stefan956.serviceUptimeMonitor.alert_service.model.NotificationChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        if (notificationServices.isEmpty()) {
            log.warn("No notification services configured, skipping alert for service: {}", request.serviceName());
            return;
        }

        for (NotificationService notificationService : notificationServices) {
            NotificationChannel channel = notificationService.getChannel();

            if (isInCooldown(request, channel)) {
                log.info("Alert for service '{}' with status {} via {} is within cooldown period, skipping",
                        request.serviceName(), request.newStatus(), channel);
                continue;
            }

            Alert alert = new Alert();
            alert.setServiceName(request.serviceName());
            alert.setOldStatus(request.oldStatus());
            alert.setNewStatus(request.newStatus());
            alert.setHttpStatusCode(request.httpStatusCode());
            alert.setChangedAt(request.changedAt());
            alert.setNotificationChannel(channel);

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
    public Page<AlertResponseDto> getAllAlerts(Pageable pageable) {
        return alertRepository.findAllByOrderByNotifiedAtDesc(pageable)
                .map(this::toDto);
    }

    @Transactional(readOnly = true)
    public List<AlertResponseDto> getAlertsByServiceName(String serviceName) {
        return alertRepository.findByServiceNameOrderByNotifiedAtDesc(serviceName)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private boolean isInCooldown(AlertRequestDto request, NotificationChannel channel) {
        Optional<Alert> lastAlert = alertRepository
                .findTopByServiceNameAndNewStatusAndNotificationChannelOrderByNotifiedAtDesc(
                        request.serviceName(), request.newStatus(), channel);

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
