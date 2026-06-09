package com.github.Stefan956.serviceUptimeMonitor.alert_service.service;

import com.github.Stefan956.serviceUptimeMonitor.alert_service.persistence.entity.Alert;
import com.github.Stefan956.serviceUptimeMonitor.alert_service.enums.NotificationChannel;

public interface NotificationService {

    void notify(Alert alert);

    NotificationChannel getChannel();
}
