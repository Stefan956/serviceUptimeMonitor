package com.github.Stefan956.serviceUptimeMonitor.dashboard_service.client;

import com.github.Stefan956.serviceUptimeMonitor.dashboard_service.dto.MonitoredServiceDto;
import com.github.Stefan956.serviceUptimeMonitor.dashboard_service.dto.ServiceStatusHistoryDto;
import com.github.Stefan956.serviceUptimeMonitor.dashboard_service.dto.ServiceStatusSummaryDto;
import com.github.Stefan956.serviceUptimeMonitor.dashboard_service.model.ServiceHealthStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MonitoringServiceClient Unit Tests")
class MonitoringServiceClientTest {

    @Mock
    private WebClient monitoringWebClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private MonitoringServiceClient client;

    @BeforeEach
    void setUp() {
        client = new MonitoringServiceClient(monitoringWebClient);
    }

    @Test
    @DisplayName("getCurrentStatuses calls correct URI and returns list")
    void getCurrentStatuses_callsCorrectUri() {
        // Given
        List<ServiceStatusSummaryDto> expected = List.of(
                new ServiceStatusSummaryDto(UUID.randomUUID(), "service-a", ServiceHealthStatus.UP, 200, 45L, LocalDateTime.now())
        );

        when(monitoringWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/api/monitoring/read/current-statuses")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(expected));

        // When
        List<ServiceStatusSummaryDto> result = client.getCurrentStatuses();

        // Then
        assertThat(result).isEqualTo(expected);
        verify(requestHeadersUriSpec).uri("/api/monitoring/read/current-statuses");
    }

    @Test
    @DisplayName("getServiceHistory calls correct URI with serviceId")
    void getServiceHistory_callsCorrectUri() {
        // Given
        UUID serviceId = UUID.randomUUID();
        List<ServiceStatusHistoryDto> expected = List.of(
                new ServiceStatusHistoryDto(UUID.randomUUID(), ServiceHealthStatus.UP, 200, 30L, LocalDateTime.now())
        );

        when(monitoringWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/api/monitoring/read/history/{serviceId}", serviceId)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(expected));

        // When
        List<ServiceStatusHistoryDto> result = client.getServiceHistory(serviceId);

        // Then
        assertThat(result).isEqualTo(expected);
        verify(requestHeadersUriSpec).uri("/api/monitoring/read/history/{serviceId}", serviceId);
    }

    @Test
    @DisplayName("getAllServices calls correct URI and returns list")
    void getAllServices_callsCorrectUri() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        List<MonitoredServiceDto> expected = List.of(
                new MonitoredServiceDto(UUID.randomUUID(), "service-a", "http://service-a.local", 60, true, now, now)
        );

        when(monitoringWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/api/monitoring/services")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(expected));

        // When
        List<MonitoredServiceDto> result = client.getAllServices();

        // Then
        assertThat(result).isEqualTo(expected);
        verify(requestHeadersUriSpec).uri("/api/monitoring/services");
    }

    @Test
    @DisplayName("getServiceById calls correct URI with id")
    void getServiceById_callsCorrectUri() {
        // Given
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        MonitoredServiceDto expected = new MonitoredServiceDto(id, "service-a", "http://service-a.local", 60, true, now, now);

        when(monitoringWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/api/monitoring/services/{id}", id)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(MonitoredServiceDto.class)).thenReturn(Mono.just(expected));

        // When
        MonitoredServiceDto result = client.getServiceById(id);

        // Then
        assertThat(result).isEqualTo(expected);
        verify(requestHeadersUriSpec).uri("/api/monitoring/services/{id}", id);
    }

    @Test
    @DisplayName("getCurrentStatuses propagates WebClient exceptions")
    void getCurrentStatuses_propagatesException() {
        // Given
        when(monitoringWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/api/monitoring/read/current-statuses")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.error(new RuntimeException("Connection refused")));

        // When / Then
        assertThatThrownBy(() -> client.getCurrentStatuses())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Connection refused");
    }
}
