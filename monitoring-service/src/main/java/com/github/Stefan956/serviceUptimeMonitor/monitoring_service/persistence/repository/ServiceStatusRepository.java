package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.persistence.repository;

import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.persistence.entity.MonitoredService;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.persistence.entity.ServiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceStatusRepository extends JpaRepository<ServiceStatus, UUID> {
    Optional<ServiceStatus> findTopByMonitoredServiceOrderByCheckedAtDesc(
            MonitoredService monitoredService
    );

    List<ServiceStatus> findByMonitoredServiceIdOrderByCheckedAtDesc(
            UUID serviceId
    );
}