package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.service;

import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.model.ServiceHealthStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthCheckService {

    private final WebClient webClient;

    public record Result(ServiceHealthStatus status, int httpStatusCode, long responseTimeMs) {}

    public Result check(String url) {
        long start = System.currentTimeMillis();
        try {
            var response = webClient
                    .get()
                    .uri(url)
                    .retrieve()
                    .toBodilessEntity()
                    .block(Duration.ofSeconds(3));

            if (response == null) {
                throw new IllegalStateException("No response received");
            }

            long responseTimeMs = System.currentTimeMillis() - start;
            return new Result(ServiceHealthStatus.UP, response.getStatusCode().value(), responseTimeMs);

        } catch (Exception e) {
            long responseTimeMs = System.currentTimeMillis() - start;
            log.warn("Health check failed for {}: {}", url, e.getMessage());
            return new Result(ServiceHealthStatus.DOWN, 0, responseTimeMs);
        }
    }
}
