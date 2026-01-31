package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.controller;

import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto.ServiceStatusHistoryDto;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto.ServiceStatusSummaryDto;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.service.MonitoringReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/monitoring/read")
@RequiredArgsConstructor
public class MonitoringReadController {

    private final MonitoringReadService monitoringReadService;

    @GetMapping("/current-statuses")
    public List<ServiceStatusSummaryDto> currentStatuses() {
        return monitoringReadService.getCurrentStatuses();
    }

    @GetMapping("histroy/{serviceId}")
    public List<ServiceStatusHistoryDto> getServiceHistoryById(@PathVariable UUID serviceId) {
        return monitoringReadService.getHistory(serviceId);

        // For Grafana you may want to use Pagination
    }

}
