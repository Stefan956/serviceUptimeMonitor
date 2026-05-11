package com.github.Stefan956.serviceUptimeMonitor.alert_service.controller;

import com.github.Stefan956.serviceUptimeMonitor.alert_service.dto.AlertRequestDto;
import com.github.Stefan956.serviceUptimeMonitor.alert_service.dto.AlertResponseDto;
import com.github.Stefan956.serviceUptimeMonitor.alert_service.model.NotificationChannel;
import com.github.Stefan956.serviceUptimeMonitor.alert_service.model.ServiceHealthStatus;
import com.github.Stefan956.serviceUptimeMonitor.alert_service.service.AlertProcessorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertController Unit Tests")
class AlertControllerTest {

    @Mock
    private AlertProcessorService alertProcessorService;

    @InjectMocks
    private AlertController alertController;

    @Test
    @DisplayName("Should delegate status change processing to service")
    void processStatusChange_shouldDelegateToService() {
        // Given
        AlertRequestDto request = new AlertRequestDto(
                UUID.randomUUID(), "test-service",
                ServiceHealthStatus.UP, ServiceHealthStatus.DOWN, 503, LocalDateTime.now()
        );

        // When
        alertController.processStatusChange(request);

        // Then
        verify(alertProcessorService).processStatusChange(request);
    }

    @Test
    @DisplayName("Should return paginated alerts from service")
    void getAllAlerts_shouldReturnServiceResult() {
        // Given
        AlertResponseDto dto1 = newResponseDto("svc-1");
        AlertResponseDto dto2 = newResponseDto("svc-2");
        Page<AlertResponseDto> page = new PageImpl<>(List.of(dto1, dto2));
        when(alertProcessorService.getAllAlerts(any(Pageable.class))).thenReturn(page);

        // When
        Page<AlertResponseDto> result = alertController.getAllAlerts(Pageable.unpaged());

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).serviceName()).isEqualTo("svc-1");
        assertThat(result.getContent().get(1).serviceName()).isEqualTo("svc-2");
    }

    @Test
    @DisplayName("Should delegate to service with correct service name")
    void getAlertsByServiceName_shouldDelegateWithCorrectServiceName() {
        // Given
        AlertResponseDto dto = newResponseDto("my-service");
        when(alertProcessorService.getAlertsByServiceName("my-service")).thenReturn(List.of(dto));

        // When
        List<AlertResponseDto> result = alertController.getAlertsByServiceName("my-service");

        // Then
        verify(alertProcessorService).getAlertsByServiceName("my-service");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).serviceName()).isEqualTo("my-service");
    }

    private AlertResponseDto newResponseDto(String serviceName) {
        return new AlertResponseDto(
                UUID.randomUUID(), serviceName,
                ServiceHealthStatus.UP, ServiceHealthStatus.DOWN,
                503, LocalDateTime.now(), LocalDateTime.now(), NotificationChannel.CONSOLE
        );
    }
}
