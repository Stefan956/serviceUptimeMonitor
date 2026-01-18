package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dao;

import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.model.MonitoredService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MonitoredServiceRepository extends JpaRepository<MonitoredService, UUID> {
    List<MonitoredService> findByEnabledTrue();
}