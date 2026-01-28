package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.service;

import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.client.AlertServiceClient;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dao.MonitoredServiceRepository;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dao.ServiceStatusRepository;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto.ServiceStatusChangeDto;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.model.MonitoredService;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.model.ServiceHealthStatus;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.model.ServiceStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonitoringService {

    private final MonitoredServiceRepository serviceRepository;
    private final ServiceStatusRepository statusRepository;
    private final WebClient webClient;
    private final AlertServiceClient alertServiceClient;


    public void checkAllServices() {
        List<MonitoredService> services = serviceRepository.findByEnabledTrue();

        log.info("Starting monitoring cycle for {} services", services.size());

        for (MonitoredService service : services) {
            checkSingleService(service);
        }

        log.info("Monitoring cycle finished");
    }

    private void checkSingleService(MonitoredService service) {
        long start = System.currentTimeMillis();

        try {
            var response = webClient
                    .get()
                    .uri(service.getUrl())
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            if (response == null) {
                throw new IllegalStateException("No response received");
            }

            HttpStatusCode statusCode = response.getStatusCode();
            long responseTime = System.currentTimeMillis() - start;

            saveStatus(
                    service,
                    ServiceHealthStatus.UP,
                    statusCode.value(),
                    responseTime
            );

            log.debug("Service {} is UP ({} ms)",
                    service.getName(), responseTime);

        } catch (Exception e) {
            log.warn("Service {} is DOWN: {}",
                    service.getName(), e.getMessage());

            saveStatus(
                    service,
                    ServiceHealthStatus.DOWN,
                    0,
                    0
            );
        }
    }

    private void saveStatus(
            MonitoredService service,
            ServiceHealthStatus currentStatus,
            int httpStatusCode,
            long responseTimeMs
    ) {

        Optional<ServiceStatus> lastStatus =
                statusRepository.findTopByMonitoredServiceOrderByCheckedAtDesc(service);

        // Detect status change
        if (lastStatus.isPresent()
                && lastStatus.get().getStatus() != currentStatus) {

            log.info("Service {} changed status from {} to {}",
                    service.getName(),
                    lastStatus.get().getStatus(),
                    currentStatus);

            // Notify Alert Service about the status change
            alertServiceClient.notifyStatusChange(
                    new ServiceStatusChangeDto(
                            service.getId(),
                            service.getName(),
                            lastStatus.get().getStatus(),
                            currentStatus,
                            LocalDateTime.now()
                    )
            );
        }

        ServiceStatus status = new ServiceStatus();
        status.setMonitoredService(service);
        status.setStatus(currentStatus);
        status.setHttpStatusCode(httpStatusCode);
        status.setResponseTimeMs(responseTimeMs);
        status.setCheckedAt(LocalDateTime.now());

        statusRepository.save(status);
    }
}