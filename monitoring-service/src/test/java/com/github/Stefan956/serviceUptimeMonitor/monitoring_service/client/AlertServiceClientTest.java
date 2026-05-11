package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.client;

import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto.ServiceStatusChangeEvent;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.model.ServiceHealthStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@DisplayName("AlertServiceClient Unit Tests")
class AlertServiceClientTest {

    private AlertServiceClient alertServiceClient;
    private MockRestServiceServer mockServer;
    private ServiceStatusChangeEvent testEvent;
    private static final String ALERT_SERVICE_URL = "http://localhost:8081";

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();
        alertServiceClient = new AlertServiceClient(restClient);
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
        mockServer.expect(requestTo(ALERT_SERVICE_URL + "/api/alerts/status-change"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK));

        alertServiceClient.notifyStatusChange(testEvent);

        mockServer.verify();
    }

    @Test
    @DisplayName("Should handle server error gracefully")
    void notifyStatusChange_shouldHandleServerError() {
        mockServer.expect(requestTo(ALERT_SERVICE_URL + "/api/alerts/status-change"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());

        alertServiceClient.notifyStatusChange(testEvent);
    }

    @Test
    @DisplayName("Should handle connection exception gracefully")
    void notifyStatusChange_shouldHandleConnectionException() {
        mockServer.expect(requestTo(ALERT_SERVICE_URL + "/api/alerts/status-change"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withException(new IOException("Connection refused")));

        alertServiceClient.notifyStatusChange(testEvent);
    }

    @Test
    @DisplayName("Should send notification with correct event data")
    void notifyStatusChange_shouldSendCorrectEventData() {
        mockServer.expect(requestTo(ALERT_SERVICE_URL + "/api/alerts/status-change"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK));

        ServiceStatusChangeEvent event = new ServiceStatusChangeEvent(
                UUID.randomUUID(),
                "Critical Service",
                ServiceHealthStatus.DOWN,
                ServiceHealthStatus.UP,
                200,
                LocalDateTime.now()
        );

        alertServiceClient.notifyStatusChange(event);

        mockServer.verify();
    }
}
