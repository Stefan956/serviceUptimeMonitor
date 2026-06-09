package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.service;

import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.client.AlertServiceClient;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.persistence.repository.MonitoredServiceRepository;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.persistence.repository.ServiceStatusRepository;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.requests.event.ServiceStatusChangeEvent;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.persistence.entity.MonitoredService;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.persistence.entity.ServiceStatus;
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
            CompletableFuture.allOf(futures
                    .toArray(CompletableFuture[]::new))
                    .join();
        }

        log.info("Monitoring cycle finished");
    }

    private boolean isDue(MonitoredService service) {
        if (service.getLastCheckedAt() != null) {
            return Duration.between(service.getLastCheckedAt(), LocalDateTime.now()).toSeconds() >= service.getCheckIntervalSeconds();
        }
        return true;
    }

    private void checkSingleService(MonitoredService service) {
        service.setLastCheckedAt(LocalDateTime.now());
        serviceRepository.save(service);

        HealthCheckService.ServiceHealthStatusResult serviceHealthStatusResult = healthCheckService.check(service.getUrl());

        log.debug("Service '{}' is {} ({} ms)",
                service.getName(), serviceHealthStatusResult.status(), serviceHealthStatusResult.responseTimeMs());

        saveStatus(service, serviceHealthStatusResult);
    }

    private void saveStatus(MonitoredService service, HealthCheckService.ServiceHealthStatusResult serviceHealthStatusResult) {
        Optional<ServiceStatus> lastStatus =
                statusRepository.findTopByMonitoredServiceOrderByCheckedAtDesc(service);

        ServiceStatus status = new ServiceStatus();
        status.setMonitoredService(service);
        status.setStatus(serviceHealthStatusResult.status());
        status.setHttpStatusCode(serviceHealthStatusResult.httpStatusCode());
        status.setResponseTimeMs(serviceHealthStatusResult.responseTimeMs());
        status.setCheckedAt(LocalDateTime.now());

        statusRepository.save(status);

        if (lastStatus.isPresent() && lastStatus.get().getStatus() != serviceHealthStatusResult.status()) {
            log.info("Service '{}' changed status from {} to {}",
                    service.getName(), lastStatus.get().getStatus(), serviceHealthStatusResult.status());

            alertServiceClient.notifyStatusChange(
                    new ServiceStatusChangeEvent(
                            service.getId(),
                            service.getName(),
                            lastStatus.get().getStatus(),
                            serviceHealthStatusResult.status(),
                            serviceHealthStatusResult.httpStatusCode(),
                            LocalDateTime.now()
                    )
            );
        }
    }
}
