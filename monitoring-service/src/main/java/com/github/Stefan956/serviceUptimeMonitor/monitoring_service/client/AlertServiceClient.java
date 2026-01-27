package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.client;

import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto.ServiceStatusChangeDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertServiceClient {

    private final WebClient webClient;

    @Value("${alert.service.url}")
    private String alertServiceUrl;

    public void notifyStatusChange(ServiceStatusChangeDto statusChange) {
        try {
            webClient.post()
                    .uri(alertServiceUrl + "/api/alerts/status-change")
                    .bodyValue(statusChange)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            log.info("Alert sent for service ID {}: status changed to {}", statusChange.id(), statusChange.newStatus());
        } catch (Exception e) {
            log.error("Failed to send alert for service ID {}: {}", statusChange.id(), e.getMessage());
        }
    }


}
