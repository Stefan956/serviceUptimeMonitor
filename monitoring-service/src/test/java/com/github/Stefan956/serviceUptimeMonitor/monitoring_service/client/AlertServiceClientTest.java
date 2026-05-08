package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.client;

import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto.ServiceStatusChangeEvent;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.model.ServiceHealthStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertServiceClient Unit Tests")
class AlertServiceClientTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private AlertServiceClient alertServiceClient;

    private ServiceStatusChangeEvent testEvent;
    private static final String ALERT_SERVICE_URL = "http://localhost:8081";

    @BeforeEach
    void setUp() {
        // Set the alert service URL using reflection
        ReflectionTestUtils.setField(alertServiceClient, "alertServiceUrl", ALERT_SERVICE_URL);

        testEvent = new ServiceStatusChangeEvent(
                UUID.randomUUID(),
                "Test Service",
                ServiceHealthStatus.UP,
                ServiceHealthStatus.DOWN,
                0,
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("Should successfully send status change notification")
    void notifyStatusChange_shouldSendNotificationSuccessfully() {
        // Given
        mockSuccessfulWebClientCall();

        // When
        alertServiceClient.notifyStatusChange(testEvent);

        // Then
        verify(webClient).post();
        verify(requestBodyUriSpec).uri(ALERT_SERVICE_URL + "/api/alerts/status-change");
        verify(requestBodySpec).bodyValue(testEvent);
        verify(responseSpec).toBodilessEntity();
    }

    @Test
    @DisplayName("Should handle WebClient exceptions gracefully")
    void notifyStatusChange_shouldHandleWebClientException() {
        // Given
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity())
                .thenReturn(Mono.error(new WebClientResponseException(
                        500,
                        "Internal Server Error",
                        null,
                        null,
                        null
                )));

        // When
        alertServiceClient.notifyStatusChange(testEvent);

        // Then - should not throw exception
        verify(webClient).post();
    }

    @Test
    @DisplayName("Should handle connection timeout gracefully")
    void notifyStatusChange_shouldHandleConnectionTimeout() {
        // Given
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity())
                .thenReturn(Mono.error(new RuntimeException("Connection timeout")));

        // When
        alertServiceClient.notifyStatusChange(testEvent);

        // Then - should not throw exception
        verify(webClient).post();
    }

    @Test
    @DisplayName("Should send notification with correct event data")
    void notifyStatusChange_shouldSendCorrectEventData() {
        // Given
        mockSuccessfulWebClientCall();

        ServiceStatusChangeEvent event = new ServiceStatusChangeEvent(
                UUID.randomUUID(),
                "Critical Service",
                ServiceHealthStatus.DOWN,
                ServiceHealthStatus.UP,
                200,
                LocalDateTime.now()
        );

        // When
        alertServiceClient.notifyStatusChange(event);

        // Then
        verify(requestBodySpec).bodyValue(event);
    }

    // Helper method
    private void mockSuccessfulWebClientCall() {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(Mono.just(new ResponseEntity<>(HttpStatus.OK)));
    }
}
