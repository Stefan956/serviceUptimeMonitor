package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dao;

import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.model.MonitoredService;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.model.ServiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceStatusRepository extends JpaRepository<ServiceStatus, UUID> {
    Optional<ServiceStatus> findTopByMonitoredServiceOrderByCheckedAtDesc(
            MonitoredService monitoredService
    );
}