package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.service;

import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.enums.ServiceHealthStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthCheckService {

    private final RestClient restClient;

    public record ServiceHealthStatusResult(ServiceHealthStatus status, int httpStatusCode, long responseTimeMs) {}

    public ServiceHealthStatusResult check(String url) {
        long start = System.currentTimeMillis();
        try {
            var response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .toBodilessEntity();

            long responseTimeMs = System.currentTimeMillis() - start;
            return new ServiceHealthStatusResult(ServiceHealthStatus.UP, response.getStatusCode().value(), responseTimeMs);

        } catch (Exception e) {
            long responseTimeMs = System.currentTimeMillis() - start;
            log.warn("Health check failed for {}: {}", url, e.getMessage());
            return new ServiceHealthStatusResult(ServiceHealthStatus.DOWN, 0, responseTimeMs);
        }
    }
}
