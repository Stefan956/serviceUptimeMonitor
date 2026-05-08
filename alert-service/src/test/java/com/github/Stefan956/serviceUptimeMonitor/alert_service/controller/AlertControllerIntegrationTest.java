package com.github.Stefan956.serviceUptimeMonitor.alert_service.controller;

import com.github.Stefan956.serviceUptimeMonitor.alert_service.dao.AlertRepository;
import com.github.Stefan956.serviceUptimeMonitor.alert_service.dto.AlertRequestDto;
import com.github.Stefan956.serviceUptimeMonitor.alert_service.dto.AlertResponseDto;
import com.github.Stefan956.serviceUptimeMonitor.alert_service.model.ServiceHealthStatus;
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
@DisplayName("AlertController Integration Tests")
class AlertControllerIntegrationTest {

    private WebTestClient webTestClient;

    @Autowired
    private AlertRepository alertRepository;

    @BeforeAll
    void initClient(@Autowired @LocalServerPort int port) {
        this.webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @BeforeEach
    void setUp() {
        alertRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/alerts/status-change returns 202 and persists alert")
    void postStatusChange_returnsAccepted() {
        // Given
        AlertRequestDto request = new AlertRequestDto(
                UUID.randomUUID(), "payment-service",
                ServiceHealthStatus.UP, ServiceHealthStatus.DOWN, 503, LocalDateTime.now()
        );

        // When / Then
        webTestClient.post()
                .uri("/api/alerts/status-change")
                .bodyValue(request)
                .exchange()
                .expectStatus().isAccepted();

        assertThat(alertRepository.findAll()).hasSize(1);
        assertThat(alertRepository.findAll().get(0).getServiceName()).isEqualTo("payment-service");
    }

    @Test
    @DisplayName("POST /api/alerts/status-change with invalid request returns 400")
    void postStatusChange_withInvalidRequest_returnsBadRequest() {
        // Given — missing required fields
        AlertRequestDto invalidRequest = new AlertRequestDto(
                null, "", null, null, 0, null
        );

        // When / Then
        webTestClient.post()
                .uri("/api/alerts/status-change")
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();

        assertThat(alertRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("POST /api/alerts/status-change respects cooldown — second identical alert is skipped")
    void postStatusChange_respectsCooldown() {
        // Given
        AlertRequestDto request = new AlertRequestDto(
                UUID.randomUUID(), "payment-service",
                ServiceHealthStatus.UP, ServiceHealthStatus.DOWN, 503, LocalDateTime.now()
        );

        // When — send twice quickly
        webTestClient.post()
                .uri("/api/alerts/status-change")
                .bodyValue(request)
                .exchange()
                .expectStatus().isAccepted();

        webTestClient.post()
                .uri("/api/alerts/status-change")
                .bodyValue(request)
                .exchange()
                .expectStatus().isAccepted();

        // Then — only 1 alert persisted (2nd was within cooldown)
        assertThat(alertRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("GET /api/alerts returns all alerts")
    void getAllAlerts_returnsAlerts() {
        // Given — seed via POST
        postAlert("service-a");
        postAlert("service-b");

        // When / Then
        webTestClient.get()
                .uri("/api/alerts")
                .exchange()
                .expectStatus().isOk()
                .expectBody(AlertResponseDto[].class)
                .value(body -> assertThat(body).hasSize(2));
    }

    @Test
    @DisplayName("GET /api/alerts returns empty list when no alerts exist")
    void getAllAlerts_returnsEmptyWhenNoAlerts() {
        webTestClient.get()
                .uri("/api/alerts")
                .exchange()
                .expectStatus().isOk()
                .expectBody(AlertResponseDto[].class)
                .value(body -> assertThat(body).isEmpty());
    }

    @Test
    @DisplayName("GET /api/alerts/service/{serviceName} returns filtered alerts")
    void getAlertsByServiceName_returnsFilteredAlerts() {
        // Given
        postAlert("payment-service");
        postAlert("auth-service");

        // When / Then
        webTestClient.get()
                .uri("/api/alerts/service/payment-service")
                .exchange()
                .expectStatus().isOk()
                .expectBody(AlertResponseDto[].class)
                .value(body -> {
                    assertThat(body).hasSize(1);
                    assertThat(body[0].serviceName()).isEqualTo("payment-service");
                });
    }

    @Test
    @DisplayName("GET /api/alerts/service/{serviceName} returns empty for unknown service")
    void getAlertsByServiceName_returnsEmptyForUnknownService() {
        // Given
        postAlert("payment-service");

        // When / Then
        webTestClient.get()
                .uri("/api/alerts/service/unknown-service")
                .exchange()
                .expectStatus().isOk()
                .expectBody(AlertResponseDto[].class)
                .value(body -> assertThat(body).isEmpty());
    }

    private void postAlert(String serviceName) {
        AlertRequestDto request = new AlertRequestDto(
                UUID.randomUUID(), serviceName,
                ServiceHealthStatus.UP, ServiceHealthStatus.DOWN, 503, LocalDateTime.now()
        );
        webTestClient.post()
                .uri("/api/alerts/status-change")
                .bodyValue(request)
                .exchange()
                .expectStatus().isAccepted();
    }
}
