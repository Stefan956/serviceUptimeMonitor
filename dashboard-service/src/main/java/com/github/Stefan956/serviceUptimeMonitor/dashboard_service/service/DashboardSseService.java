package com.github.Stefan956.serviceUptimeMonitor.dashboard_service.service;

import com.github.Stefan956.serviceUptimeMonitor.dashboard_service.client.MonitoringServiceClient;
import com.github.Stefan956.serviceUptimeMonitor.dashboard_service.dto.ServiceStatusSummaryDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class DashboardSseService {

    private final MonitoringServiceClient monitoringServiceClient;
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public DashboardSseService(MonitoringServiceClient monitoringServiceClient) {
        this.monitoringServiceClient = monitoringServiceClient;
    }

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L); // no timeout
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));

        log.info("New SSE subscriber connected. Total subscribers: {}", emitters.size());
        return emitter;
    }

    @Scheduled(fixedDelayString = "${dashboard.polling.interval-ms}")
    public void pollAndBroadcast() {
        if (emitters.isEmpty()) {
            return;
        }

        try {
            List<ServiceStatusSummaryDto> statuses = monitoringServiceClient.getCurrentStatuses();
            log.debug("Broadcasting status update to {} subscribers", emitters.size());

            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("status-update")
                            .data(statuses));
                } catch (IOException e) {
                    log.debug("Removing disconnected SSE subscriber");
                    emitter.completeWithError(e);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to poll monitoring-service for SSE broadcast: {}", e.getMessage());
        }
    }
}
