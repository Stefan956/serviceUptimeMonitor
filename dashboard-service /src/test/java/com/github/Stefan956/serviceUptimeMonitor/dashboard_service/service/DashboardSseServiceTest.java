package com.github.Stefan956.serviceUptimeMonitor.dashboard_service.service;

import com.github.Stefan956.serviceUptimeMonitor.dashboard_service.client.MonitoringServiceClient;
import com.github.Stefan956.serviceUptimeMonitor.dashboard_service.dto.ServiceStatusSummaryDto;
import com.github.Stefan956.serviceUptimeMonitor.dashboard_service.model.ServiceHealthStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardSseService Unit Tests")
class DashboardSseServiceTest {

    @Mock
    private MonitoringServiceClient monitoringServiceClient;

    @InjectMocks
    private DashboardSseService dashboardSseService;

    @Test
    @DisplayName("subscribe returns a non-null SseEmitter")
    void subscribe_returnsEmitter() {
        // When
        SseEmitter emitter = dashboardSseService.subscribe();

        // Then
        assertThat(emitter).isNotNull();
    }

    @Test
    @DisplayName("pollAndBroadcast does not call client when no subscribers")
    void pollAndBroadcast_skipsWhenNoSubscribers() {
        // When
        dashboardSseService.pollAndBroadcast();

        // Then
        verifyNoInteractions(monitoringServiceClient);
    }

    @Test
    @DisplayName("pollAndBroadcast fetches statuses when subscribers exist")
    void pollAndBroadcast_fetchesStatusesWithSubscribers() {
        // Given
        dashboardSseService.subscribe();
        List<ServiceStatusSummaryDto> statuses = List.of(
                new ServiceStatusSummaryDto(UUID.randomUUID(), "service-a", ServiceHealthStatus.UP, 200, 45L, LocalDateTime.now())
        );
        when(monitoringServiceClient.getCurrentStatuses()).thenReturn(statuses);

        // When
        dashboardSseService.pollAndBroadcast();

        // Then
        verify(monitoringServiceClient).getCurrentStatuses();
    }

    @Test
    @DisplayName("pollAndBroadcast handles client exception gracefully")
    void pollAndBroadcast_handlesClientException() {
        // Given
        dashboardSseService.subscribe();
        when(monitoringServiceClient.getCurrentStatuses()).thenThrow(new RuntimeException("Connection refused"));

        // When — should not throw
        dashboardSseService.pollAndBroadcast();

        // Then
        verify(monitoringServiceClient).getCurrentStatuses();
    }
}
