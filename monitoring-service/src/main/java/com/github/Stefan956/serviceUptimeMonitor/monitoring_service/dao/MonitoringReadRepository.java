package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dao;

import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto.ServiceStatusSummaryDto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MonitoringReadRepository {
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
            JOIN ms.serviceStatuses ss ON ss.monitoredService.id = ms.id
            WHERE ss.checkedAt = (
                SELECT MAX(ss2.checkedAt)
                FROM ServiceStatus ss2
                WHERE ss2.monitoredService.id = ms.id
            )
            """)
    List<ServiceStatusSummaryDto> findCurrentStatusPerService();
}
