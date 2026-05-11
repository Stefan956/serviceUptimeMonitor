package com.github.Stefan956.serviceUptimeMonitor.dashboard_service.service;

import com.github.Stefan956.serviceUptimeMonitor.dashboard_service.client.MonitoringServiceClient;
import com.github.Stefan956.serviceUptimeMonitor.dashboard_service.dto.DashboardOverviewDto;
import com.github.Stefan956.serviceUptimeMonitor.dashboard_service.dto.MonitoredServiceDto;
import com.github.Stefan956.serviceUptimeMonitor.dashboard_service.dto.ServiceStatusHistoryDto;
import com.github.Stefan956.serviceUptimeMonitor.dashboard_service.dto.ServiceStatusSummaryDto;
import com.github.Stefan956.serviceUptimeMonitor.dashboard_service.model.ServiceHealthStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final MonitoringServiceClient monitoringServiceClient;

    public DashboardOverviewDto getOverview() {
        List<ServiceStatusSummaryDto> statuses = monitoringServiceClient.getCurrentStatuses();

        int total = statuses.size();
        int up = (int) statuses.stream()
                .filter(s -> s.status() == ServiceHealthStatus.UP)
                .count();
        int down = total - up;

        return new DashboardOverviewDto(total, up, down, statuses);
    }

    public List<ServiceStatusSummaryDto> getCurrentStatuses() {
        return monitoringServiceClient.getCurrentStatuses();
    }

    public List<ServiceStatusHistoryDto> getServiceHistory(UUID serviceId) {
        return monitoringServiceClient.getServiceHistory(serviceId);
    }

    public List<MonitoredServiceDto> getAllMonitoredServices() {
        return monitoringServiceClient.getAllServices();
    }
}
