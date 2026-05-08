package com.github.Stefan956.serviceUptimeMonitor.alert_service.service;

import com.github.Stefan956.serviceUptimeMonitor.alert_service.model.Alert;
import com.github.Stefan956.serviceUptimeMonitor.alert_service.model.NotificationChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(name = "alert.console.enabled", havingValue = "true")
public class  ConsoleAlertService implements NotificationService {

    @Override
    public void notify(Alert alert) {
        log.warn("ALERT: Service '{}' status changed from {} to {} | HTTP: {} | Changed at: {}",
                alert.getServiceName(),
                alert.getOldStatus(),
                alert.getNewStatus(),
                alert.getHttpStatusCode(),
                alert.getChangedAt());
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.CONSOLE;
    }
}
