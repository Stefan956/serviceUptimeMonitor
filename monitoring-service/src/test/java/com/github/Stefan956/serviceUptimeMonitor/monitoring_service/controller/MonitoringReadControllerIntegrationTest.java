package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.controller;

import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dao.MonitoredServiceRepository;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dao.ServiceStatusRepository;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto.ServiceStatusHistoryDto;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto.ServiceStatusSummaryDto;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.model.MonitoredService;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.model.ServiceHealthStatus;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.model.ServiceStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MonitoringReadControllerIntegrationTest {

    private WebTestClient webTestClient;

    @Autowired
    private MonitoredServiceRepository serviceRepository;

    @Autowired
    private ServiceStatusRepository statusRepository;

    @BeforeAll
    void initClient(@Autowired @LocalServerPort int port) {
        this.webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    private MonitoredService testService;

    @BeforeEach
    void setUp() {
        statusRepository.deleteAll();
        serviceRepository.deleteAll();

        testService = new MonitoredService();
        testService.setName("Test Service");
        testService.setUrl("http://test.com/health");
        testService.setCheckIntervalSeconds(60);
        testService.setEnabled(true);
        testService.setCreatedAt(LocalDateTime.now());
        testService = serviceRepository.save(testService);
    }

    @Test
    @DisplayName("GET /api/monitoring/read/current-statuses returns current status summaries")
    void currentStatuses_returnsSummaries() {
        LocalDateTime now = LocalDateTime.now();
        createStatus(testService, ServiceHealthStatus.DOWN, 0, 0, now.minusMinutes(10));
        createStatus(testService, ServiceHealthStatus.UP, 200, 85, now);

        webTestClient.get()
                .uri("/api/monitoring/read/current-statuses")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ServiceStatusSummaryDto[].class)
                .value(body -> {
                    assertThat(body).hasSize(1);
                    assertThat(body[0].serviceName()).isEqualTo("Test Service");
                    assertThat(body[0].status()).isEqualTo(ServiceHealthStatus.UP);
                    assertThat(body[0].httpStatusCode()).isEqualTo(200);
                });
    }

    @Test
    @DisplayName("GET /api/monitoring/read/current-statuses returns empty when no statuses")
    void currentStatuses_returnsEmptyWhenNoStatuses() {
        webTestClient.get()
                .uri("/api/monitoring/read/current-statuses")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ServiceStatusSummaryDto[].class)
                .value(body -> assertThat(body).isEmpty());
    }

    @Test
    @DisplayName("GET /api/monitoring/read/current-statuses returns multiple services")
    void currentStatuses_returnsMultipleServices() {
        MonitoredService service2 = new MonitoredService();
        service2.setName("Service B");
        service2.setUrl("http://b.com/health");
        service2.setCheckIntervalSeconds(30);
        service2.setEnabled(true);
        service2.setCreatedAt(LocalDateTime.now());
        service2 = serviceRepository.save(service2);

        LocalDateTime now = LocalDateTime.now();
        createStatus(testService, ServiceHealthStatus.UP, 200, 50, now);
        createStatus(service2, ServiceHealthStatus.DOWN, 0, 0, now);

        webTestClient.get()
                .uri("/api/monitoring/read/current-statuses")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ServiceStatusSummaryDto[].class)
                .value(body -> assertThat(body).hasSize(2));
    }

    @Test
    @DisplayName("GET /api/monitoring/read/history/{serviceId} returns status history ordered by checkedAt desc")
    void getHistory_returnsStatusHistory() {
        LocalDateTime now = LocalDateTime.now();
        createStatus(testService, ServiceHealthStatus.UP, 200, 50, now.minusMinutes(10));
        createStatus(testService, ServiceHealthStatus.DOWN, 0, 0, now.minusMinutes(5));
        createStatus(testService, ServiceHealthStatus.UP, 200, 80, now);

        webTestClient.get()
                .uri("/api/monitoring/read/history/" + testService.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(ServiceStatusHistoryDto[].class)
                .value(body -> {
                    assertThat(body).hasSize(3);
                    // Ordered by checkedAt DESC — newest first
                    assertThat(body[0].status()).isEqualTo(ServiceHealthStatus.UP);
                    assertThat(body[1].status()).isEqualTo(ServiceHealthStatus.DOWN);
                    assertThat(body[2].status()).isEqualTo(ServiceHealthStatus.UP);
                });
    }

    @Test
    @DisplayName("GET /api/monitoring/read/history/{serviceId} with non-existent ID returns 404")
    void getHistory_nonExistentId_returnsNotFound() {
        webTestClient.get()
                .uri("/api/monitoring/read/history/" + UUID.randomUUID())
                .exchange()
                .expectStatus().isNotFound();
    }

    private ServiceStatus createStatus(MonitoredService service, ServiceHealthStatus healthStatus,
                                        int httpStatusCode, long responseTimeMs, LocalDateTime checkedAt) {
        ServiceStatus status = new ServiceStatus();
        status.setMonitoredService(service);
        status.setStatus(healthStatus);
        status.setHttpStatusCode(httpStatusCode);
        status.setResponseTimeMs(responseTimeMs);
        status.setCheckedAt(checkedAt);
        return statusRepository.save(status);
    }
}
