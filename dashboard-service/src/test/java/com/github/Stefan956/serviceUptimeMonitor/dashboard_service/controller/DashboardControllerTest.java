package com.github.Stefan956.serviceUptimeMonitor.dashboard_service.controller;

import com.github.Stefan956.serviceUptimeMonitor.dashboard_service.dto.DashboardOverviewDto;
import com.github.Stefan956.serviceUptimeMonitor.dashboard_service.dto.MonitoredServiceDto;
import com.github.Stefan956.serviceUptimeMonitor.dashboard_service.dto.ServiceStatusHistoryDto;
import com.github.Stefan956.serviceUptimeMonitor.dashboard_service.dto.ServiceStatusSummaryDto;
import com.github.Stefan956.serviceUptimeMonitor.dashboard_service.model.ServiceHealthStatus;
import com.github.Stefan956.serviceUptimeMonitor.dashboard_service.service.DashboardService;
import com.github.Stefan956.serviceUptimeMonitor.dashboard_service.service.DashboardSseService;
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
@DisplayName("DashboardController Unit Tests")
class DashboardControllerTest {

    @Mock
    private DashboardService dashboardService;

    @Mock
    private DashboardSseService dashboardSseService;

    @InjectMocks
    private DashboardController dashboardController;

    @Test
    @DisplayName("getOverview delegates to DashboardService")
    void getOverview_delegatesToService() {
        // Given
        List<ServiceStatusSummaryDto> statuses = List.of(summaryUp("service-a"));
        DashboardOverviewDto expected = new DashboardOverviewDto(1, 1, 0, statuses);
        when(dashboardService.getOverview()).thenReturn(expected);

        // When
        DashboardOverviewDto result = dashboardController.getOverview();

        // Then
        assertThat(result).isEqualTo(expected);
        verify(dashboardService).getOverview();
    }

    @Test
    @DisplayName("getCurrentStatuses delegates to DashboardService")
    void getCurrentStatuses_delegatesToService() {
        // Given
        List<ServiceStatusSummaryDto> expected = List.of(summaryUp("service-a"));
        when(dashboardService.getCurrentStatuses()).thenReturn(expected);

        // When
        List<ServiceStatusSummaryDto> result = dashboardController.getCurrentStatuses();

        // Then
        assertThat(result).isEqualTo(expected);
        verify(dashboardService).getCurrentStatuses();
    }

    @Test
    @DisplayName("getAllServices delegates to DashboardService")
    void getAllServices_delegatesToService() {
        // Given
        List<MonitoredServiceDto> expected = List.of(
                monitoredService("payment-service", "http://payment.local/health")
        );
        when(dashboardService.getAllMonitoredServices()).thenReturn(expected);

        // When
        List<MonitoredServiceDto> result = dashboardController.getAllServices();

        // Then
        assertThat(result).isEqualTo(expected);
        verify(dashboardService).getAllMonitoredServices();
    }

    @Test
    @DisplayName("getServiceHistory delegates to DashboardService with correct serviceId")
    void getServiceHistory_delegatesToService() {
        // Given
        UUID serviceId = UUID.randomUUID();
        List<ServiceStatusHistoryDto> expected = List.of(historyEntry(ServiceHealthStatus.UP, 200, 50L));
        when(dashboardService.getServiceHistory(serviceId)).thenReturn(expected);

        // When
        List<ServiceStatusHistoryDto> result = dashboardController.getServiceHistory(serviceId);

        // Then
        assertThat(result).isEqualTo(expected);
        verify(dashboardService).getServiceHistory(serviceId);
    }

    @Test
    @DisplayName("streamStatusUpdates delegates to DashboardSseService")
    void streamStatusUpdates_delegatesToSseService() {
        // Given
        SseEmitter expected = new SseEmitter();
        when(dashboardSseService.subscribe()).thenReturn(expected);

        // When
        SseEmitter result = dashboardController.streamStatusUpdates();

        // Then
        assertThat(result).isEqualTo(expected);
        verify(dashboardSseService).subscribe();
    }

    // Helper methods
    private ServiceStatusSummaryDto summaryUp(String serviceName) {
        return new ServiceStatusSummaryDto(UUID.randomUUID(), serviceName, ServiceHealthStatus.UP, 200, 45L, LocalDateTime.now());
    }

    private ServiceStatusHistoryDto historyEntry(ServiceHealthStatus status, int httpStatusCode, long responseTimeMs) {
        return new ServiceStatusHistoryDto(UUID.randomUUID(), status, httpStatusCode, responseTimeMs, LocalDateTime.now());
    }

    private MonitoredServiceDto monitoredService(String name, String url) {
        LocalDateTime now = LocalDateTime.now();
        return new MonitoredServiceDto(UUID.randomUUID(), name, url, 60, true, now, now);
    }
}
