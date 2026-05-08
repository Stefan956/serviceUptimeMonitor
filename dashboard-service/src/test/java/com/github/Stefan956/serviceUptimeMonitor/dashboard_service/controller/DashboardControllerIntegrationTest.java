package com.github.Stefan956.serviceUptimeMonitor.dashboard_service.controller;

import com.github.Stefan956.serviceUptimeMonitor.dashboard_service.client.MonitoringServiceClient;
import com.github.Stefan956.serviceUptimeMonitor.dashboard_service.dto.DashboardOverviewDto;
import com.github.Stefan956.serviceUptimeMonitor.dashboard_service.dto.MonitoredServiceDto;
import com.github.Stefan956.serviceUptimeMonitor.dashboard_service.dto.ServiceStatusHistoryDto;
import com.github.Stefan956.serviceUptimeMonitor.dashboard_service.dto.ServiceStatusSummaryDto;
import com.github.Stefan956.serviceUptimeMonitor.dashboard_service.model.ServiceHealthStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("DashboardController Integration Tests")
class DashboardControllerIntegrationTest {

    private WebTestClient webTestClient;

    @MockitoBean
    private MonitoringServiceClient monitoringServiceClient;

    @BeforeAll
    void initClient(@Autowired @LocalServerPort int port) {
        this.webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    // ==================== GET /api/dashboard/overview ====================

    @Test
    @DisplayName("GET /api/dashboard/overview returns 200 with aggregated overview")
    void getOverview_returnsAggregatedOverview() {
        // Given
        List<ServiceStatusSummaryDto> statuses = List.of(
                summaryUp("payment-service"),
                summaryUp("auth-service"),
                summaryDown("order-service")
        );
        when(monitoringServiceClient.getCurrentStatuses()).thenReturn(statuses);

        // When / Then
        webTestClient.get()
                .uri("/api/dashboard/overview")
                .exchange()
                .expectStatus().isOk()
                .expectBody(DashboardOverviewDto.class)
                .value(overview -> {
                    assertThat(overview.totalServices()).isEqualTo(3);
                    assertThat(overview.servicesUp()).isEqualTo(2);
                    assertThat(overview.servicesDown()).isEqualTo(1);
                    assertThat(overview.statuses()).hasSize(3);
                });
    }

    @Test
    @DisplayName("GET /api/dashboard/overview returns zeros when no services exist")
    void getOverview_returnsZerosWhenEmpty() {
        // Given
        when(monitoringServiceClient.getCurrentStatuses()).thenReturn(Collections.emptyList());

        // When / Then
        webTestClient.get()
                .uri("/api/dashboard/overview")
                .exchange()
                .expectStatus().isOk()
                .expectBody(DashboardOverviewDto.class)
                .value(overview -> {
                    assertThat(overview.totalServices()).isZero();
                    assertThat(overview.servicesUp()).isZero();
                    assertThat(overview.servicesDown()).isZero();
                    assertThat(overview.statuses()).isEmpty();
                });
    }

    // ==================== GET /api/dashboard/statuses ====================

    @Test
    @DisplayName("GET /api/dashboard/statuses returns 200 with current statuses")
    void getStatuses_returnsCurrentStatuses() {
        // Given
        List<ServiceStatusSummaryDto> statuses = List.of(
                summaryUp("payment-service"),
                summaryDown("order-service")
        );
        when(monitoringServiceClient.getCurrentStatuses()).thenReturn(statuses);

        // When / Then
        webTestClient.get()
                .uri("/api/dashboard/statuses")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ServiceStatusSummaryDto[].class)
                .value(body -> {
                    assertThat(body).hasSize(2);
                    assertThat(body[0].serviceName()).isEqualTo("payment-service");
                    assertThat(body[0].status()).isEqualTo(ServiceHealthStatus.UP);
                    assertThat(body[1].serviceName()).isEqualTo("order-service");
                    assertThat(body[1].status()).isEqualTo(ServiceHealthStatus.DOWN);
                });
    }

    @Test
    @DisplayName("GET /api/dashboard/statuses returns empty array when no services")
    void getStatuses_returnsEmptyWhenNoServices() {
        // Given
        when(monitoringServiceClient.getCurrentStatuses()).thenReturn(Collections.emptyList());

        // When / Then
        webTestClient.get()
                .uri("/api/dashboard/statuses")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ServiceStatusSummaryDto[].class)
                .value(body -> assertThat(body).isEmpty());
    }

    // ==================== GET /api/dashboard/services ====================

    @Test
    @DisplayName("GET /api/dashboard/services returns 200 with all monitored services")
    void getServices_returnsAllServices() {
        // Given
        List<MonitoredServiceDto> services = List.of(
                monitoredService("payment-service", "http://payment.local/health"),
                monitoredService("auth-service", "http://auth.local/health")
        );
        when(monitoringServiceClient.getAllServices()).thenReturn(services);

        // When / Then
        webTestClient.get()
                .uri("/api/dashboard/services")
                .exchange()
                .expectStatus().isOk()
                .expectBody(MonitoredServiceDto[].class)
                .value(body -> {
                    assertThat(body).hasSize(2);
                    assertThat(body[0].name()).isEqualTo("payment-service");
                    assertThat(body[1].name()).isEqualTo("auth-service");
                });
    }

    @Test
    @DisplayName("GET /api/dashboard/services returns empty array when no services registered")
    void getServices_returnsEmptyWhenNone() {
        // Given
        when(monitoringServiceClient.getAllServices()).thenReturn(Collections.emptyList());

        // When / Then
        webTestClient.get()
                .uri("/api/dashboard/services")
                .exchange()
                .expectStatus().isOk()
                .expectBody(MonitoredServiceDto[].class)
                .value(body -> assertThat(body).isEmpty());
    }

    // ==================== GET /api/dashboard/history/{serviceId} ====================

    @Test
    @DisplayName("GET /api/dashboard/history/{serviceId} returns 200 with service history")
    void getHistory_returnsServiceHistory() {
        // Given
        UUID serviceId = UUID.randomUUID();
        List<ServiceStatusHistoryDto> history = List.of(
                historyEntry("UP", 200, 42L),
                historyEntry("DOWN", 0, 0L),
                historyEntry("UP", 200, 55L)
        );
        when(monitoringServiceClient.getServiceHistory(serviceId)).thenReturn(history);

        // When / Then
        webTestClient.get()
                .uri("/api/dashboard/history/" + serviceId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ServiceStatusHistoryDto[].class)
                .value(body -> {
                    assertThat(body).hasSize(3);
                    assertThat(body[0].status()).isEqualTo(ServiceHealthStatus.UP);
                    assertThat(body[0].httpStatusCode()).isEqualTo(200);
                    assertThat(body[1].status()).isEqualTo(ServiceHealthStatus.DOWN);
                });
    }

    @Test
    @DisplayName("GET /api/dashboard/history/{serviceId} returns empty when no history")
    void getHistory_returnsEmptyWhenNoHistory() {
        // Given
        UUID serviceId = UUID.randomUUID();
        when(monitoringServiceClient.getServiceHistory(serviceId)).thenReturn(Collections.emptyList());

        // When / Then
        webTestClient.get()
                .uri("/api/dashboard/history/" + serviceId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ServiceStatusHistoryDto[].class)
                .value(body -> assertThat(body).isEmpty());
    }

    // NOTE: SSE endpoint (/api/dashboard/stream) is not integration-tested here
    // because WebTestClient.bindToServer() blocks on infinite SSE streams.
    // SSE behaviour is covered by DashboardSseServiceTest and DashboardControllerTest (unit tests).

    // Helper methods
    private ServiceStatusSummaryDto summaryUp(String serviceName) {
        return new ServiceStatusSummaryDto(UUID.randomUUID(), serviceName, ServiceHealthStatus.UP, 200, 45L, LocalDateTime.now());
    }

    private ServiceStatusSummaryDto summaryDown(String serviceName) {
        return new ServiceStatusSummaryDto(UUID.randomUUID(), serviceName, ServiceHealthStatus.DOWN, 0, 0L, LocalDateTime.now());
    }

    private ServiceStatusHistoryDto historyEntry(String status, int httpStatusCode, long responseTimeMs) {
        return new ServiceStatusHistoryDto(UUID.randomUUID(), ServiceHealthStatus.valueOf(status), httpStatusCode, responseTimeMs, LocalDateTime.now());
    }

    private MonitoredServiceDto monitoredService(String name, String url) {
        LocalDateTime now = LocalDateTime.now();
        return new MonitoredServiceDto(UUID.randomUUID(), name, url, 60, true, now, now);
    }
}
