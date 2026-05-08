package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.service;

import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dao.MonitoredServiceRepository;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dao.MonitoringReadRepository;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dao.ServiceStatusRepository;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto.ServiceStatusHistoryDto;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto.ServiceStatusSummaryDto;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.model.MonitoredService;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.model.ServiceHealthStatus;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.model.ServiceStatus;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MonitoringReadService Unit Tests")
class MonitoringReadServiceTest {

    @Mock
    private MonitoredServiceRepository monitoredServiceRepository;

    @Mock
    private ServiceStatusRepository serviceStatusRepository;

    @Mock
    private MonitoringReadRepository readRepository;

    @InjectMocks
    private MonitoringReadService monitoringReadService;

    private UUID serviceId;
    private MonitoredService testService;
    private ServiceStatus testStatus;

    @BeforeEach
    void setUp() {
        serviceId = UUID.randomUUID();

        testService = new MonitoredService();
        testService.setId(serviceId);
        testService.setName("Test Service");
        testService.setUrl("http://example.com/health");
        testService.setCheckIntervalSeconds(60);
        testService.setEnabled(true);
        testService.setCreatedAt(LocalDateTime.now());

        testStatus = new ServiceStatus();
        testStatus.setId(UUID.randomUUID());
        testStatus.setMonitoredService(testService);
        testStatus.setStatus(ServiceHealthStatus.UP);
        testStatus.setHttpStatusCode(200);
        testStatus.setResponseTimeMs(150);
        testStatus.setCheckedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should get current statuses for all services")
    void getCurrentStatuses_shouldReturnAllCurrentStatuses() {
        // Given
        ServiceStatusSummaryDto summary1 = new ServiceStatusSummaryDto(
                UUID.randomUUID(),
                "Service 1",
                ServiceHealthStatus.UP,
                200,
                100L,
                LocalDateTime.now()
        );

        ServiceStatusSummaryDto summary2 = new ServiceStatusSummaryDto(
                UUID.randomUUID(),
                "Service 2",
                ServiceHealthStatus.DOWN,
                0,
                0L,
                LocalDateTime.now()
        );

        when(readRepository.findCurrentStatusPerService()).thenReturn(List.of(summary1, summary2));

        // When
        List<ServiceStatusSummaryDto> result = monitoringReadService.getCurrentStatuses();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).serviceName()).isEqualTo("Service 1");
        assertThat(result.get(0).status()).isEqualTo(ServiceHealthStatus.UP);
        assertThat(result.get(1).serviceName()).isEqualTo("Service 2");
        assertThat(result.get(1).status()).isEqualTo(ServiceHealthStatus.DOWN);

        verify(readRepository).findCurrentStatusPerService();
    }

    @Test
    @DisplayName("Should return empty list when no current statuses exist")
    void getCurrentStatuses_shouldReturnEmptyList_whenNoStatuses() {
        // Given
        when(readRepository.findCurrentStatusPerService()).thenReturn(List.of());

        // When
        List<ServiceStatusSummaryDto> result = monitoringReadService.getCurrentStatuses();

        // Then
        assertThat(result).isEmpty();
        verify(readRepository).findCurrentStatusPerService();
    }

    @Test
    @DisplayName("Should get history for a specific service")
    void getHistory_shouldReturnServiceHistory() {
        // Given
        ServiceStatus status1 = createServiceStatus(ServiceHealthStatus.UP, 200, 100);
        ServiceStatus status2 = createServiceStatus(ServiceHealthStatus.UP, 200, 120);
        ServiceStatus status3 = createServiceStatus(ServiceHealthStatus.DOWN, 0, 0);

        when(monitoredServiceRepository.existsById(serviceId)).thenReturn(true);
        when(serviceStatusRepository.findByMonitoredServiceIdOrderByCheckedAtDesc(serviceId))
                .thenReturn(List.of(status3, status2, status1));

        // When
        List<ServiceStatusHistoryDto> result = monitoringReadService.getHistory(serviceId);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).status()).isEqualTo(ServiceHealthStatus.DOWN);
        assertThat(result.get(1).status()).isEqualTo(ServiceHealthStatus.UP);
        assertThat(result.get(1).httpStatusCode()).isEqualTo(200);
        assertThat(result.get(1).responseTimeMs()).isEqualTo(120);

        verify(monitoredServiceRepository).existsById(serviceId);
        verify(serviceStatusRepository).findByMonitoredServiceIdOrderByCheckedAtDesc(serviceId);
    }

    @Test
    @DisplayName("Should throw exception when getting history for non-existent service")
    void getHistory_shouldThrowException_whenServiceNotFound() {
        // Given
        when(monitoredServiceRepository.existsById(serviceId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> monitoringReadService.getHistory(serviceId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Service not found");

        verify(monitoredServiceRepository).existsById(serviceId);
        verify(serviceStatusRepository, never()).findByMonitoredServiceIdOrderByCheckedAtDesc(any());
    }

    @Test
    @DisplayName("Should return empty history when service has no status records")
    void getHistory_shouldReturnEmptyList_whenNoHistory() {
        // Given
        when(monitoredServiceRepository.existsById(serviceId)).thenReturn(true);
        when(serviceStatusRepository.findByMonitoredServiceIdOrderByCheckedAtDesc(serviceId))
                .thenReturn(List.of());

        // When
        List<ServiceStatusHistoryDto> result = monitoringReadService.getHistory(serviceId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should correctly map ServiceStatus to ServiceStatusHistoryDto")
    void getHistory_shouldCorrectlyMapStatusToDto() {
        // Given
        LocalDateTime checkedAt = LocalDateTime.now();
        testStatus.setCheckedAt(checkedAt);

        when(monitoredServiceRepository.existsById(serviceId)).thenReturn(true);
        when(serviceStatusRepository.findByMonitoredServiceIdOrderByCheckedAtDesc(serviceId))
                .thenReturn(List.of(testStatus));

        // When
        List<ServiceStatusHistoryDto> result = monitoringReadService.getHistory(serviceId);

        // Then
        assertThat(result).hasSize(1);
        ServiceStatusHistoryDto dto = result.get(0);
        assertThat(dto.id()).isEqualTo(testStatus.getId());
        assertThat(dto.status()).isEqualTo(testStatus.getStatus());
        assertThat(dto.httpStatusCode()).isEqualTo(testStatus.getHttpStatusCode());
        assertThat(dto.responseTimeMs()).isEqualTo(testStatus.getResponseTimeMs());
        assertThat(dto.checkedAt()).isEqualTo(testStatus.getCheckedAt());
    }

    // Helper method
    private ServiceStatus createServiceStatus(ServiceHealthStatus status, int httpCode, long responseTime) {
        ServiceStatus serviceStatus = new ServiceStatus();
        serviceStatus.setId(UUID.randomUUID());
        serviceStatus.setMonitoredService(testService);
        serviceStatus.setStatus(status);
        serviceStatus.setHttpStatusCode(httpCode);
        serviceStatus.setResponseTimeMs(responseTime);
        serviceStatus.setCheckedAt(LocalDateTime.now());
        return serviceStatus;
    }
}
