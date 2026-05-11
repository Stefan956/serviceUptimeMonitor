package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dao;

import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto.ServiceStatusSummaryDto;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.model.ServiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MonitoringReadRepository extends JpaRepository<ServiceStatus, UUID> {
    @Query("""
            SELECT new com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto.ServiceStatusSummaryDto(
                ms.id,
                ms.name,
                ss.status,
                ss.httpStatusCode,
                ss.responseTimeMs,
                ss.checkedAt
            )
            FROM MonitoredService ms
            JOIN ms.serviceStatuses ss
            WHERE NOT EXISTS (
                SELECT 1 FROM ServiceStatus ss2
                WHERE ss2.monitoredService.id = ms.id
                AND (ss2.checkedAt > ss.checkedAt
                     OR (ss2.checkedAt = ss.checkedAt AND ss2.id > ss.id))
            )
            """)
    List<ServiceStatusSummaryDto> findCurrentStatusPerService();
}
