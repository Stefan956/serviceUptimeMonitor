package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.controller;

import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dao.MonitoredServiceRepository;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto.MonitoredServiceRequestDto;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto.MonitoredServiceResponseDto;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.model.MonitoredService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
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
class MonitoredServiceControllerIntegrationTest {

    private WebTestClient webTestClient;

    @Autowired
    private MonitoredServiceRepository repository;

    @BeforeAll
    void initClient(@Autowired @LocalServerPort int port) {
        this.webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    private MonitoredService existingService;

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        existingService = new MonitoredService();
        existingService.setName("Existing Service");
        existingService.setUrl("http://existing.com/health");
        existingService.setCheckIntervalSeconds(60);
        existingService.setEnabled(true);
        existingService.setCreatedAt(LocalDateTime.now());
        existingService = repository.save(existingService);
    }

    @Test
    @DisplayName("POST /api/monitoring/services creates a new service and returns 201")
    void create_returnsCreatedService() {
        MonitoredServiceRequestDto request = new MonitoredServiceRequestDto(
                "New Service", "http://new.com/health", 30
        );

        webTestClient.post()
                .uri("/api/monitoring/services")
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(MonitoredServiceResponseDto.class)
                .value(body -> {
                    assertThat(body.name()).isEqualTo("New Service");
                    assertThat(body.url()).isEqualTo("http://new.com/health");
                    assertThat(body.checkIntervalSeconds()).isEqualTo(30);
                    assertThat(body.enabled()).isTrue();
                    assertThat(body.id()).isNotNull();
                });

        assertThat(repository.findAll()).hasSize(2);
    }

    @Test
    @DisplayName("PUT /api/monitoring/services/{id} updates an existing service")
    void update_returnsUpdatedService() {
        MonitoredServiceRequestDto request = new MonitoredServiceRequestDto(
                "Updated Name", "http://updated.com/health", 120
        );

        webTestClient.put()
                .uri("/api/monitoring/services/" + existingService.getId())
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(MonitoredServiceResponseDto.class)
                .value(body -> {
                    assertThat(body.name()).isEqualTo("Updated Name");
                    assertThat(body.url()).isEqualTo("http://updated.com/health");
                    assertThat(body.checkIntervalSeconds()).isEqualTo(120);
                });
    }

    @Test
    @DisplayName("PUT /api/monitoring/services/{id} with non-existent ID returns 404")
    void update_nonExistentId_returnsNotFound() {
        MonitoredServiceRequestDto request = new MonitoredServiceRequestDto(
                "Updated", "http://updated.com/health", 60
        );

        webTestClient.put()
                .uri("/api/monitoring/services/" + UUID.randomUUID())
                .bodyValue(request)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("PATCH /api/monitoring/services/{id}/enable returns 204")
    void enable_returnsNoContent() {
        existingService.setEnabled(false);
        repository.save(existingService);

        webTestClient.patch()
                .uri("/api/monitoring/services/" + existingService.getId() + "/enable")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("PATCH /api/monitoring/services/{id}/disable returns 204")
    void disable_returnsNoContent() {
        webTestClient.patch()
                .uri("/api/monitoring/services/" + existingService.getId() + "/disable")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("GET /api/monitoring/services/{id} returns the service")
    void getById_returnsService() {
        webTestClient.get()
                .uri("/api/monitoring/services/" + existingService.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(MonitoredServiceResponseDto.class)
                .value(body -> {
                    assertThat(body.name()).isEqualTo("Existing Service");
                    assertThat(body.url()).isEqualTo("http://existing.com/health");
                    assertThat(body.checkIntervalSeconds()).isEqualTo(60);
                    assertThat(body.enabled()).isTrue();
                });
    }

    @Test
    @DisplayName("GET /api/monitoring/services/{id} with non-existent ID returns 404")
    void getById_nonExistentId_returnsNotFound() {
        webTestClient.get()
                .uri("/api/monitoring/services/" + UUID.randomUUID())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("GET /api/monitoring/services returns all services")
    void getAll_returnsList() {
        MonitoredService second = new MonitoredService();
        second.setName("Second Service");
        second.setUrl("http://second.com/health");
        second.setCheckIntervalSeconds(30);
        second.setEnabled(false);
        second.setCreatedAt(LocalDateTime.now());
        repository.save(second);

        webTestClient.get()
                .uri("/api/monitoring/services")
                .exchange()
                .expectStatus().isOk()
                .expectBody(MonitoredServiceResponseDto[].class)
                .value(body -> assertThat(body).hasSize(2));
    }

    @Test
    @DisplayName("GET /api/monitoring/services returns empty list when no services exist")
    void getAll_returnsEmptyList() {
        repository.deleteAll();

        webTestClient.get()
                .uri("/api/monitoring/services")
                .exchange()
                .expectStatus().isOk()
                .expectBody(MonitoredServiceResponseDto[].class)
                .value(body -> assertThat(body).isEmpty());
    }

    @Test
    @DisplayName("DELETE /api/monitoring/services/{id} removes the service and returns 204")
    void delete_returnsNoContent() {
        webTestClient.delete()
                .uri("/api/monitoring/services/" + existingService.getId())
                .exchange()
                .expectStatus().isNoContent();

        assertThat(repository.findById(existingService.getId())).isEmpty();
    }

    @Test
    @DisplayName("DELETE /api/monitoring/services/{id} with non-existent ID returns 404")
    void delete_nonExistentId_returnsNotFound() {
        webTestClient.delete()
                .uri("/api/monitoring/services/" + UUID.randomUUID())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("POST /api/monitoring/services with blank name returns 400 with field errors")
    void create_blankName_returns400WithErrors() {
        MonitoredServiceRequestDto invalid = new MonitoredServiceRequestDto(
                "", "http://valid.com/health", 30
        );

        webTestClient.post()
                .uri("/api/monitoring/services")
                .bodyValue(invalid)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Validation Error")
                .jsonPath("$.errors").isArray();
    }

    @Test
    @DisplayName("POST /api/monitoring/services with invalid URL returns 400 with field errors")
    void create_invalidUrl_returns400WithErrors() {
        MonitoredServiceRequestDto invalid = new MonitoredServiceRequestDto(
                "My Service", "not-a-url", 30
        );

        webTestClient.post()
                .uri("/api/monitoring/services")
                .bodyValue(invalid)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Validation Error")
                .jsonPath("$.errors").isArray();
    }

    @Test
    @DisplayName("POST /api/monitoring/services with checkIntervalSeconds below minimum returns 400")
    void create_intervalBelowMinimum_returns400WithErrors() {
        MonitoredServiceRequestDto invalid = new MonitoredServiceRequestDto(
                "My Service", "http://valid.com/health", 1
        );

        webTestClient.post()
                .uri("/api/monitoring/services")
                .bodyValue(invalid)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Validation Error")
                .jsonPath("$.errors").isArray();
    }
}
