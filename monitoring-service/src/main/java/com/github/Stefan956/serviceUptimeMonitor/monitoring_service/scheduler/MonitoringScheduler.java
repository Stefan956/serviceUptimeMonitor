//TODO: triggers a run of the monitoring logic every 30 seconds
package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.scheduler;

import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.service.MonitoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "monitoring.scheduler",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class MonitoringScheduler {

    private final MonitoringService monitoringService;

    @Scheduled(fixedDelayString = "${monitoring.scheduler.fixed-delay-ms}")
    public void runMonitoringCycle() {
        log.info("Starting monitoring cycle...");
        monitoringService.checkAllServices();
        log.info("Monitoring cycle completed.");
    }
}