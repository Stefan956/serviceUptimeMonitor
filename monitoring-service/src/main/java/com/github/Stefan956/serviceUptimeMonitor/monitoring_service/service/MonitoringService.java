package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.service;

import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.client.AlertServiceClient;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dao.MonitoredServiceRepository;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dao.ServiceStatusRepository;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto.ServiceStatusChangeEvent;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.model.MonitoredService;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.model.ServiceHealthStatus;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.model.ServiceStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonitoringService {

    private final MonitoredServiceRepository serviceRepository;
    private final ServiceStatusRepository statusRepository;
    private final HealthCheckService healthCheckService;
    private final AlertServiceClient alertServiceClient;

    public void checkAllServices() {
        List<MonitoredService> services = serviceRepository.findByEnabledTrue();

        log.info("Starting monitoring cycle for {} services", services.size());

        List<CompletableFuture<Void>> futures = services.stream()
                .filter(service -> {
                    if (!isDue(service)) {
                        log.debug("Skipping service '{}' — not due yet", service.getName());
                        return false;
                    }
                    return true;
                })
                .map(service -> CompletableFuture.runAsync(() -> checkSingleService(service)))
                .toList();

        if (!futures.isEmpty()) {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }

        log.info("Monitoring cycle finished");
    }

    private boolean isDue(MonitoredService service) {
        if (service.getLastCheckedAt() == null) {
            return true;
        }
        return Duration.between(service.getLastCheckedAt(), LocalDateTime.now()).toSeconds()
                >= service.getCheckIntervalSeconds();
    }

    private void checkSingleService(MonitoredService service) {
        service.setLastCheckedAt(LocalDateTime.now());
        serviceRepository.save(service);

        HealthCheckService.Result result = healthCheckService.check(service.getUrl());

        log.debug("Service '{}' is {} ({} ms)",
                service.getName(), result.status(), result.responseTimeMs());

        saveStatus(service, result);
    }

    private void saveStatus(MonitoredService service, HealthCheckService.Result result) {
        Optional<ServiceStatus> lastStatus =
                statusRepository.findTopByMonitoredServiceOrderByCheckedAtDesc(service);

        ServiceStatus status = new ServiceStatus();
        status.setMonitoredService(service);
        status.setStatus(result.status());
        status.setHttpStatusCode(result.httpStatusCode());
        status.setResponseTimeMs(result.responseTimeMs());
        status.setCheckedAt(LocalDateTime.now());

        statusRepository.save(status);

        if (lastStatus.isPresent() && lastStatus.get().getStatus() != result.status()) {
            log.info("Service '{}' changed status from {} to {}",
                    service.getName(), lastStatus.get().getStatus(), result.status());

            alertServiceClient.notifyStatusChange(
                    new ServiceStatusChangeEvent(
                            service.getId(),
                            service.getName(),
                            lastStatus.get().getStatus(),
                            result.status(),
                            result.httpStatusCode(),
                            LocalDateTime.now()
                    )
            );
        }
    }
}
