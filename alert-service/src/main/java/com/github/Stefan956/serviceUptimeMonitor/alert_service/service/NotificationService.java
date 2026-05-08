package com.github.Stefan956.serviceUptimeMonitor.alert_service.service;

import com.github.Stefan956.serviceUptimeMonitor.alert_service.model.Alert;
import com.github.Stefan956.serviceUptimeMonitor.alert_service.model.NotificationChannel;

public interface NotificationService {

    void notify(Alert alert);

    NotificationChannel getChannel();
}
