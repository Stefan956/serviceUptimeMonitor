package com.github.Stefan956.serviceUptimeMonitor.alert_service.service;

import com.github.Stefan956.serviceUptimeMonitor.alert_service.dao.AlertRepository;
import com.github.Stefan956.serviceUptimeMonitor.alert_service.dto.AlertRequestDto;
import com.github.Stefan956.serviceUptimeMonitor.alert_service.dto.AlertResponseDto;
import com.github.Stefan956.serviceUptimeMonitor.alert_service.model.Alert;
import com.github.Stefan956.serviceUptimeMonitor.alert_service.model.NotificationChannel;
import com.github.Stefan956.serviceUptimeMonitor.alert_service.model.ServiceHealthStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertProcessorService Unit Tests")
class AlertProcessorServiceTest {

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private NotificationService notificationService;

    private AlertProcessorService alertProcessorService;

    private AlertRequestDto defaultRequest;

    @BeforeEach
    void setUp() {
        alertProcessorService = new AlertProcessorService(alertRepository, List.of(notificationService));
        ReflectionTestUtils.setField(alertProcessorService, "cooldownMs", 300000L);

        defaultRequest = new AlertRequestDto(
                UUID.randomUUID(), "test-service",
                ServiceHealthStatus.UP, ServiceHealthStatus.DOWN, 503, LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("Should save alert and notify when not in cooldown")
    void processStatusChange_shouldSaveAlertAndNotify_whenNotInCooldown() {
        // Given
        when(notificationService.getChannel()).thenReturn(NotificationChannel.CONSOLE);
        when(alertRepository.findTopByServiceNameAndNewStatusOrderByNotifiedAtDesc(
                defaultRequest.serviceName(), defaultRequest.newStatus()))
                .thenReturn(Optional.empty());
        when(alertRepository.save(any(Alert.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        alertProcessorService.processStatusChange(defaultRequest);

        // Then
        ArgumentCaptor<Alert> alertCaptor = ArgumentCaptor.forClass(Alert.class);
        verify(alertRepository).save(alertCaptor.capture());

        Alert savedAlert = alertCaptor.getValue();
        assertThat(savedAlert.getServiceName()).isEqualTo(defaultRequest.serviceName());
        assertThat(savedAlert.getOldStatus()).isEqualTo(defaultRequest.oldStatus());
        assertThat(savedAlert.getNewStatus()).isEqualTo(defaultRequest.newStatus());
        assertThat(savedAlert.getHttpStatusCode()).isEqualTo(defaultRequest.httpStatusCode());
        assertThat(savedAlert.getChangedAt()).isEqualTo(defaultRequest.changedAt());
        assertThat(savedAlert.getNotificationChannel()).isEqualTo(NotificationChannel.CONSOLE);

        verify(notificationService).notify(any(Alert.class));
    }

    @Test
    @DisplayName("Should skip notification when in cooldown period")
    void processStatusChange_shouldSkipNotification_whenInCooldown() {
        // Given
        Alert recentAlert = createAlert("test-service", LocalDateTime.now().minusSeconds(30));
        when(alertRepository.findTopByServiceNameAndNewStatusOrderByNotifiedAtDesc(
                defaultRequest.serviceName(), defaultRequest.newStatus()))
                .thenReturn(Optional.of(recentAlert));

        // When
        alertProcessorService.processStatusChange(defaultRequest);

        // Then
        verify(alertRepository, never()).save(any(Alert.class));
        verify(notificationService, never()).notify(any(Alert.class));
    }

    @Test
    @DisplayName("Should notify when cooldown has expired")
    void processStatusChange_shouldNotify_whenCooldownExpired() {
        // Given
        when(notificationService.getChannel()).thenReturn(NotificationChannel.CONSOLE);
        Alert oldAlert = createAlert("test-service", LocalDateTime.now().minusMinutes(10));
        when(alertRepository.findTopByServiceNameAndNewStatusOrderByNotifiedAtDesc(
                defaultRequest.serviceName(), defaultRequest.newStatus()))
                .thenReturn(Optional.of(oldAlert));
        when(alertRepository.save(any(Alert.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        alertProcessorService.processStatusChange(defaultRequest);

        // Then
        verify(alertRepository).save(any(Alert.class));
        verify(notificationService).notify(any(Alert.class));
    }

    @Test
    @DisplayName("Should save alert for each notification service")
    void processStatusChange_shouldSaveAlertForEachNotificationService() {
        // Given
        when(notificationService.getChannel()).thenReturn(NotificationChannel.CONSOLE);
        NotificationService emailService = mock(NotificationService.class);
        when(emailService.getChannel()).thenReturn(NotificationChannel.EMAIL);

        alertProcessorService = new AlertProcessorService(
                alertRepository, List.of(notificationService, emailService));
        ReflectionTestUtils.setField(alertProcessorService, "cooldownMs", 300000L);

        when(alertRepository.findTopByServiceNameAndNewStatusOrderByNotifiedAtDesc(
                defaultRequest.serviceName(), defaultRequest.newStatus()))
                .thenReturn(Optional.empty());
        when(alertRepository.save(any(Alert.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        alertProcessorService.processStatusChange(defaultRequest);

        // Then
        verify(alertRepository, times(2)).save(any(Alert.class));
        verify(notificationService).notify(any(Alert.class));
        verify(emailService).notify(any(Alert.class));
    }

    @Test
    @DisplayName("Should return all alerts mapped to DTOs")
    void getAllAlerts_shouldReturnAllAlertsMappedToDto() {
        // Given
        Alert alert1 = createAlert("service-1", LocalDateTime.now());
        Alert alert2 = createAlert("service-2", LocalDateTime.now());
        when(alertRepository.findAllByOrderByNotifiedAtDesc()).thenReturn(List.of(alert1, alert2));

        // When
        List<AlertResponseDto> result = alertProcessorService.getAllAlerts();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).serviceName()).isEqualTo("service-1");
        assertThat(result.get(1).serviceName()).isEqualTo("service-2");
    }

    @Test
    @DisplayName("Should return empty list when no alerts exist")
    void getAllAlerts_shouldReturnEmptyList_whenNoAlerts() {
        // Given
        when(alertRepository.findAllByOrderByNotifiedAtDesc()).thenReturn(Collections.emptyList());

        // When
        List<AlertResponseDto> result = alertProcessorService.getAllAlerts();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return filtered alerts by service name")
    void getAlertsByServiceName_shouldReturnFilteredAlerts() {
        // Given
        Alert alert = createAlert("my-service", LocalDateTime.now());
        when(alertRepository.findByServiceNameOrderByNotifiedAtDesc("my-service"))
                .thenReturn(List.of(alert));

        // When
        List<AlertResponseDto> result = alertProcessorService.getAlertsByServiceName("my-service");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).serviceName()).isEqualTo("my-service");
    }

    @Test
    @DisplayName("Should return empty list when no alerts for service")
    void getAlertsByServiceName_shouldReturnEmptyList_whenNoAlertsForService() {
        // Given
        when(alertRepository.findByServiceNameOrderByNotifiedAtDesc("unknown-service"))
                .thenReturn(Collections.emptyList());

        // When
        List<AlertResponseDto> result = alertProcessorService.getAlertsByServiceName("unknown-service");

        // Then
        assertThat(result).isEmpty();
    }

    // Helper method
    private Alert createAlert(String serviceName, LocalDateTime notifiedAt) {
        Alert alert = new Alert();
        alert.setServiceName(serviceName);
        alert.setOldStatus(ServiceHealthStatus.UP);
        alert.setNewStatus(ServiceHealthStatus.DOWN);
        alert.setHttpStatusCode(503);
        alert.setChangedAt(LocalDateTime.now());
        alert.setNotifiedAt(notifiedAt);
        alert.setNotificationChannel(NotificationChannel.CONSOLE);
        return alert;
    }
}
