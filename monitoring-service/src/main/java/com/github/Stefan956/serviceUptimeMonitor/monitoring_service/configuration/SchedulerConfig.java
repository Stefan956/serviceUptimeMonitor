package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.configuration;

import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.service.MonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SchedulerConfig {

    private final MonitoringService monitoringService;

    @Scheduled(fixedDelayString = "${monitoring.scheduler.fixed-delay-ms}")
    public void runChecks() {
        monitoringService.checkAllServices();
    }
}
