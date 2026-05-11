package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.client;

import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto.ServiceStatusChangeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertServiceClient {

    private final RestClient restClient;

    @Value("${alert.service.url}")
    private String alertServiceUrl;

    public void notifyStatusChange(ServiceStatusChangeEvent statusChange) {
        try {
            restClient.post()
                    .uri(alertServiceUrl + "/api/alerts/status-change")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(statusChange)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Alert sent for service ID {}: status changed to {}", statusChange.serviceId(), statusChange.newStatus());
        } catch (Exception e) {
            log.error("Failed to send alert for service ID {}: {}", statusChange.serviceId(), e.getMessage());
        }
    }
}
