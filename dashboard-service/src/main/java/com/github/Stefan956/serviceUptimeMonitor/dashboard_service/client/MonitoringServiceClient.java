package com.github.Stefan956.serviceUptimeMonitor.dashboard_service.client;

import com.github.Stefan956.serviceUptimeMonitor.dashboard_service.requests.response.MonitoredServiceDto;
import com.github.Stefan956.serviceUptimeMonitor.dashboard_service.requests.response.ServiceStatusHistoryDto;
import com.github.Stefan956.serviceUptimeMonitor.dashboard_service.requests.response.ServiceStatusSummaryDto;
import com.github.Stefan956.serviceUptimeMonitor.dashboard_service.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonitoringServiceClient {

    private final WebClient monitoringWebClient;

    public List<ServiceStatusSummaryDto> getCurrentStatuses() {
        log.debug("Fetching current statuses from monitoring-service");
        return monitoringWebClient.get()
                .uri(Constants.MONITORING_SERVICE_CURRENT_STATUSES)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ServiceStatusSummaryDto>>() {})
                .defaultIfEmpty(List.of())
                .block(Constants.DEFAULT_TIMEOUT);
    }

    public List<ServiceStatusHistoryDto> getServiceHistory(UUID serviceId) {
        log.debug("Fetching history for service {} from monitoring-service", serviceId);
        return monitoringWebClient.get()
                .uri(Constants.MONITORING_SERVICE_HISTORY, serviceId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ServiceStatusHistoryDto>>() {})
                .defaultIfEmpty(List.of())
                .block(Constants.DEFAULT_TIMEOUT);
    }

    public List<MonitoredServiceDto> getAllServices() {
        log.debug("Fetching all services from monitoring-service");
        return monitoringWebClient.get()
                .uri(Constants.MONITORING_SERVICES_ALL_SERVICES)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<MonitoredServiceDto>>() {})
                .defaultIfEmpty(List.of())
                .block(Constants.DEFAULT_TIMEOUT);
    }

    public MonitoredServiceDto getServiceById(UUID id) {
        log.debug("Fetching service {} from monitoring-service", id);
        return monitoringWebClient.get()
                .uri(Constants.MONITORING_SERVICES_FETCHED_SERVICES_BY_ID, id)
                .retrieve()
                .bodyToMono(MonitoredServiceDto.class)
                .block(Constants.DEFAULT_TIMEOUT);
    }
}
