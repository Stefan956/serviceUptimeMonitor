package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.service;

import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.client.AlertServiceClient;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dao.MonitoredServiceRepository;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dao.ServiceStatusRepository;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto.ServiceStatusChangeEvent;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.model.MonitoredService;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.model.ServiceHealthStatus;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.model.ServiceStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MonitoringService Unit Tests")
class MonitoringServiceTest {

    @Mock
    private MonitoredServiceRepository serviceRepository;

    @Mock
    private ServiceStatusRepository statusRepository;

    @Mock
    private HealthCheckService healthCheckService;

    @Mock
    private AlertServiceClient alertServiceClient;

    @InjectMocks
    private MonitoringService monitoringService;

    private MonitoredService testService;

    @BeforeEach
    void setUp() {
        testService = new MonitoredService();
        testService.setId(UUID.randomUUID());
        testService.setName("Test Service");
        testService.setUrl("http://example.com/health");
        testService.setCheckIntervalSeconds(60);
        testService.setEnabled(true);
        testService.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should check all enabled services")
    void checkAllServices_shouldCheckEnabledServicesOnly() {
        // Given
        MonitoredService service1 = createService("Service 1", "http://service1.com");
        MonitoredService service2 = createService("Service 2", "http://service2.com");

        when(serviceRepository.findByEnabledTrue()).thenReturn(List.of(service1, service2));
        mockSuccessfulHealthCheck();
        when(statusRepository.findTopByMonitoredServiceOrderByCheckedAtDesc(any())).thenReturn(Optional.empty());

        // When
        monitoringService.checkAllServices();

        // Then
        verify(serviceRepository).findByEnabledTrue();
        verify(statusRepository, times(2)).save(any(ServiceStatus.class));
    }

    @Test
    @DisplayName("Should save UP status when health check succeeds")
    void checkSingleService_shouldSaveUpStatus_whenServiceRespondsSuccessfully() {
        // Given
        mockSuccessfulHealthCheck();
        when(serviceRepository.findByEnabledTrue()).thenReturn(List.of(testService));
        when(statusRepository.findTopByMonitoredServiceOrderByCheckedAtDesc(testService))
                .thenReturn(Optional.empty());

        ArgumentCaptor<ServiceStatus> statusCaptor = ArgumentCaptor.forClass(ServiceStatus.class);

        // When
        monitoringService.checkAllServices();

        // Then
        verify(statusRepository).save(statusCaptor.capture());
        ServiceStatus savedStatus = statusCaptor.getValue();

        assertThat(savedStatus.getStatus()).isEqualTo(ServiceHealthStatus.UP);
        assertThat(savedStatus.getHttpStatusCode()).isEqualTo(200);
        assertThat(savedStatus.getResponseTimeMs()).isGreaterThanOrEqualTo(0);
        assertThat(savedStatus.getMonitoredService()).isEqualTo(testService);
    }

    @Test
    @DisplayName("Should save DOWN status when health check fails")
    void checkSingleService_shouldSaveDownStatus_whenServiceIsDown() {
        // Given
        when(serviceRepository.findByEnabledTrue()).thenReturn(List.of(testService));
        when(statusRepository.findTopByMonitoredServiceOrderByCheckedAtDesc(testService))
                .thenReturn(Optional.empty());
        when(healthCheckService.check(testService.getUrl()))
                .thenReturn(new HealthCheckService.Result(ServiceHealthStatus.DOWN, 0, 0L));

        ArgumentCaptor<ServiceStatus> statusCaptor = ArgumentCaptor.forClass(ServiceStatus.class);

        // When
        monitoringService.checkAllServices();

        // Then
        verify(statusRepository).save(statusCaptor.capture());
        ServiceStatus savedStatus = statusCaptor.getValue();

        assertThat(savedStatus.getStatus()).isEqualTo(ServiceHealthStatus.DOWN);
        assertThat(savedStatus.getHttpStatusCode()).isEqualTo(0);
        assertThat(savedStatus.getResponseTimeMs()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should send alert when status changes from UP to DOWN")
    void checkSingleService_shouldSendAlert_whenStatusChangesFromUpToDown() {
        // Given
        ServiceStatus previousStatus = new ServiceStatus();
        previousStatus.setStatus(ServiceHealthStatus.UP);
        previousStatus.setMonitoredService(testService);
        previousStatus.setCheckedAt(LocalDateTime.now().minusMinutes(5));

        when(serviceRepository.findByEnabledTrue()).thenReturn(List.of(testService));
        when(statusRepository.findTopByMonitoredServiceOrderByCheckedAtDesc(testService))
                .thenReturn(Optional.of(previousStatus));
        when(healthCheckService.check(testService.getUrl()))
                .thenReturn(new HealthCheckService.Result(ServiceHealthStatus.DOWN, 0, 0L));

        ArgumentCaptor<ServiceStatusChangeEvent> eventCaptor = ArgumentCaptor.forClass(ServiceStatusChangeEvent.class);

        // When
        monitoringService.checkAllServices();

        // Then
        verify(alertServiceClient).notifyStatusChange(eventCaptor.capture());
        ServiceStatusChangeEvent event = eventCaptor.getValue();

        assertThat(event.serviceId()).isEqualTo(testService.getId());
        assertThat(event.serviceName()).isEqualTo(testService.getName());
        assertThat(event.oldStatus()).isEqualTo(ServiceHealthStatus.UP);
        assertThat(event.newStatus()).isEqualTo(ServiceHealthStatus.DOWN);
    }

    @Test
    @DisplayName("Should send alert when status changes from DOWN to UP")
    void checkSingleService_shouldSendAlert_whenStatusChangesFromDownToUp() {
        // Given
        ServiceStatus previousStatus = new ServiceStatus();
        previousStatus.setStatus(ServiceHealthStatus.DOWN);
        previousStatus.setMonitoredService(testService);
        previousStatus.setCheckedAt(LocalDateTime.now().minusMinutes(5));

        when(serviceRepository.findByEnabledTrue()).thenReturn(List.of(testService));
        when(statusRepository.findTopByMonitoredServiceOrderByCheckedAtDesc(testService))
                .thenReturn(Optional.of(previousStatus));
        mockSuccessfulHealthCheck();

        ArgumentCaptor<ServiceStatusChangeEvent> eventCaptor = ArgumentCaptor.forClass(ServiceStatusChangeEvent.class);

        // When
        monitoringService.checkAllServices();

        // Then
        verify(alertServiceClient).notifyStatusChange(eventCaptor.capture());
        ServiceStatusChangeEvent event = eventCaptor.getValue();

        assertThat(event.serviceId()).isEqualTo(testService.getId());
        assertThat(event.oldStatus()).isEqualTo(ServiceHealthStatus.DOWN);
        assertThat(event.newStatus()).isEqualTo(ServiceHealthStatus.UP);
    }

    @Test
    @DisplayName("Should not send alert when status remains the same")
    void checkSingleService_shouldNotSendAlert_whenStatusRemainsTheSame() {
        // Given
        ServiceStatus previousStatus = new ServiceStatus();
        previousStatus.setStatus(ServiceHealthStatus.UP);
        previousStatus.setMonitoredService(testService);
        previousStatus.setCheckedAt(LocalDateTime.now().minusMinutes(5));

        when(serviceRepository.findByEnabledTrue()).thenReturn(List.of(testService));
        when(statusRepository.findTopByMonitoredServiceOrderByCheckedAtDesc(testService))
                .thenReturn(Optional.of(previousStatus));
        mockSuccessfulHealthCheck();

        // When
        monitoringService.checkAllServices();

        // Then
        verify(alertServiceClient, never()).notifyStatusChange(any());
    }

    @Test
    @DisplayName("Should not send alert when there is no previous status")
    void checkSingleService_shouldNotSendAlert_whenNoPreviousStatus() {
        // Given
        when(serviceRepository.findByEnabledTrue()).thenReturn(List.of(testService));
        when(statusRepository.findTopByMonitoredServiceOrderByCheckedAtDesc(testService))
                .thenReturn(Optional.empty());
        mockSuccessfulHealthCheck();

        // When
        monitoringService.checkAllServices();

        // Then
        verify(alertServiceClient, never()).notifyStatusChange(any());
    }

    @Test
    @DisplayName("Should skip a service whose interval has not elapsed yet")
    void checkAllServices_shouldSkipServiceNotDueYet() {
        // Given — check the service once so it is recorded in lastCheckedAt
        testService.setCheckIntervalSeconds(300);
        when(serviceRepository.findByEnabledTrue()).thenReturn(List.of(testService));
        mockSuccessfulHealthCheck();
        when(statusRepository.findTopByMonitoredServiceOrderByCheckedAtDesc(any())).thenReturn(Optional.empty());

        monitoringService.checkAllServices(); // first tick — service is due

        // When — second tick immediately after (interval = 300 s, so not due)
        monitoringService.checkAllServices();

        // Then — health check called only once across both ticks
        verify(healthCheckService, times(1)).check(anyString());
    }

    // Helper methods
    private void mockSuccessfulHealthCheck() {
        when(healthCheckService.check(anyString()))
                .thenReturn(new HealthCheckService.Result(ServiceHealthStatus.UP, 200, 100L));
    }

    private MonitoredService createService(String name, String url) {
        MonitoredService service = new MonitoredService();
        service.setId(UUID.randomUUID());
        service.setName(name);
        service.setUrl(url);
        service.setCheckIntervalSeconds(60);
        service.setEnabled(true);
        service.setCreatedAt(LocalDateTime.now());
        return service;
    }
}
