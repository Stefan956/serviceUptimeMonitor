package com.github.Stefan956.serviceUptimeMonitor.dashboard_service.service;

import com.github.Stefan956.serviceUptimeMonitor.dashboard_service.client.MonitoringServiceClient;
import com.github.Stefan956.serviceUptimeMonitor.dashboard_service.dto.DashboardOverviewDto;
import com.github.Stefan956.serviceUptimeMonitor.dashboard_service.dto.MonitoredServiceDto;
import com.github.Stefan956.serviceUptimeMonitor.dashboard_service.dto.ServiceStatusHistoryDto;
import com.github.Stefan956.serviceUptimeMonitor.dashboard_service.dto.ServiceStatusSummaryDto;
import com.github.Stefan956.serviceUptimeMonitor.dashboard_service.model.ServiceHealthStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardService Unit Tests")
class DashboardServiceTest {

    @Mock
    private MonitoringServiceClient monitoringServiceClient;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    @DisplayName("getOverview returns correct counts when services are mixed UP and DOWN")
    void getOverview_returnsCorrectCounts() {
        // Given
        List<ServiceStatusSummaryDto> statuses = List.of(
                summaryUp("service-a"),
                summaryUp("service-b"),
                summaryDown("service-c")
        );
        when(monitoringServiceClient.getCurrentStatuses()).thenReturn(statuses);

        // When
        DashboardOverviewDto overview = dashboardService.getOverview();

        // Then
        assertThat(overview.totalServices()).isEqualTo(3);
        assertThat(overview.servicesUp()).isEqualTo(2);
        assertThat(overview.servicesDown()).isEqualTo(1);
        assertThat(overview.statuses()).hasSize(3);
    }

    @Test
    @DisplayName("getOverview returns all zeros when no services exist")
    void getOverview_returnsZerosWhenEmpty() {
        // Given
        when(monitoringServiceClient.getCurrentStatuses()).thenReturn(Collections.emptyList());

        // When
        DashboardOverviewDto overview = dashboardService.getOverview();

        // Then
        assertThat(overview.totalServices()).isZero();
        assertThat(overview.servicesUp()).isZero();
        assertThat(overview.servicesDown()).isZero();
        assertThat(overview.statuses()).isEmpty();
    }

    @Test
    @DisplayName("getOverview counts all services as UP when none are DOWN")
    void getOverview_allServicesUp() {
        // Given
        List<ServiceStatusSummaryDto> statuses = List.of(
                summaryUp("service-a"),
                summaryUp("service-b")
        );
        when(monitoringServiceClient.getCurrentStatuses()).thenReturn(statuses);

        // When
        DashboardOverviewDto overview = dashboardService.getOverview();

        // Then
        assertThat(overview.totalServices()).isEqualTo(2);
        assertThat(overview.servicesUp()).isEqualTo(2);
        assertThat(overview.servicesDown()).isZero();
    }

    @Test
    @DisplayName("getOverview counts all services as DOWN when none are UP")
    void getOverview_allServicesDown() {
        // Given
        List<ServiceStatusSummaryDto> statuses = List.of(
                summaryDown("service-a"),
                summaryDown("service-b")
        );
        when(monitoringServiceClient.getCurrentStatuses()).thenReturn(statuses);

        // When
        DashboardOverviewDto overview = dashboardService.getOverview();

        // Then
        assertThat(overview.totalServices()).isEqualTo(2);
        assertThat(overview.servicesUp()).isZero();
        assertThat(overview.servicesDown()).isEqualTo(2);
    }

    @Test
    @DisplayName("getCurrentStatuses delegates to client")
    void getCurrentStatuses_delegatesToClient() {
        // Given
        List<ServiceStatusSummaryDto> expected = List.of(summaryUp("service-a"));
        when(monitoringServiceClient.getCurrentStatuses()).thenReturn(expected);

        // When
        List<ServiceStatusSummaryDto> result = dashboardService.getCurrentStatuses();

        // Then
        assertThat(result).isEqualTo(expected);
        verify(monitoringServiceClient).getCurrentStatuses();
    }

    @Test
    @DisplayName("getServiceHistory delegates to client with correct serviceId")
    void getServiceHistory_delegatesToClient() {
        // Given
        UUID serviceId = UUID.randomUUID();
        List<ServiceStatusHistoryDto> expected = List.of(
                historyEntry(ServiceHealthStatus.UP, 200, 50L),
                historyEntry(ServiceHealthStatus.DOWN, 0, 0L)
        );
        when(monitoringServiceClient.getServiceHistory(serviceId)).thenReturn(expected);

        // When
        List<ServiceStatusHistoryDto> result = dashboardService.getServiceHistory(serviceId);

        // Then
        assertThat(result).isEqualTo(expected);
        verify(monitoringServiceClient).getServiceHistory(serviceId);
    }

    @Test
    @DisplayName("getAllMonitoredServices delegates to client")
    void getAllMonitoredServices_delegatesToClient() {
        // Given
        List<MonitoredServiceDto> expected = List.of(
                monitoredService("payment-service", "http://payment.local/health")
        );
        when(monitoringServiceClient.getAllServices()).thenReturn(expected);

        // When
        List<MonitoredServiceDto> result = dashboardService.getAllMonitoredServices();

        // Then
        assertThat(result).isEqualTo(expected);
        verify(monitoringServiceClient).getAllServices();
    }

    // Helper methods
    private ServiceStatusSummaryDto summaryUp(String serviceName) {
        return new ServiceStatusSummaryDto(UUID.randomUUID(), serviceName, ServiceHealthStatus.UP, 200, 45L, LocalDateTime.now());
    }

    private ServiceStatusSummaryDto summaryDown(String serviceName) {
        return new ServiceStatusSummaryDto(UUID.randomUUID(), serviceName, ServiceHealthStatus.DOWN, 0, 0L, LocalDateTime.now());
    }

    private ServiceStatusHistoryDto historyEntry(ServiceHealthStatus status, int httpStatusCode, long responseTimeMs) {
        return new ServiceStatusHistoryDto(UUID.randomUUID(), status, httpStatusCode, responseTimeMs, LocalDateTime.now());
    }

    private MonitoredServiceDto monitoredService(String name, String url) {
        LocalDateTime now = LocalDateTime.now();
        return new MonitoredServiceDto(UUID.randomUUID(), name, url, 60, true, now, now);
    }
}
