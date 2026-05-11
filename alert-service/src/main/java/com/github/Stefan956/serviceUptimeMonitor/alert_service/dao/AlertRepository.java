package com.github.Stefan956.serviceUptimeMonitor.alert_service.dao;

import com.github.Stefan956.serviceUptimeMonitor.alert_service.model.Alert;
import com.github.Stefan956.serviceUptimeMonitor.alert_service.model.NotificationChannel;
import com.github.Stefan956.serviceUptimeMonitor.alert_service.model.ServiceHealthStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlertRepository extends JpaRepository<Alert, UUID> {

    List<Alert> findByServiceNameOrderByNotifiedAtDesc(String serviceName);

    Page<Alert> findAllByOrderByNotifiedAtDesc(Pageable pageable);

    Optional<Alert> findTopByServiceNameAndNewStatusAndNotificationChannelOrderByNotifiedAtDesc(
            String serviceName, ServiceHealthStatus newStatus, NotificationChannel channel);
}
