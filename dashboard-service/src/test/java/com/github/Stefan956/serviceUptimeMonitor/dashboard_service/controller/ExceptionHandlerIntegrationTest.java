package com.github.Stefan956.serviceUptimeMonitor.dashboard_service.controller;

import com.github.Stefan956.serviceUptimeMonitor.dashboard_service.client.MonitoringServiceClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.net.ConnectException;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("ExceptionHandler Integration Tests")
class ExceptionHandlerIntegrationTest {

    private WebTestClient webTestClient;

    @MockitoBean
    private MonitoringServiceClient monitoringServiceClient;

    @BeforeAll
    void initClient(@Autowired @LocalServerPort int port) {
        this.webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    @DisplayName("Returns 404 when monitoring-service responds with 404")
    void monitoringService404_returnsProblemDetail404() {
        // Given
        WebClientResponseException notFound = WebClientResponseException.create(
                404,
                "Not Found",
                HttpHeaders.EMPTY,
                new byte[0],
                null
        );
        when(monitoringServiceClient.getCurrentStatuses()).thenThrow(notFound);

        // When / Then
        webTestClient.get()
                .uri("/api/dashboard/statuses")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Monitoring Service Error")
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.detail").value(detail ->
                        assertThat((String) detail).contains("Not Found"));
    }

    @Test
    @DisplayName("Returns 500 when monitoring-service responds with 500")
    void monitoringService500_returnsProblemDetail500() {
        // Given
        WebClientResponseException serverError = WebClientResponseException.create(
                500,
                "Internal Server Error",
                HttpHeaders.EMPTY,
                new byte[0],
                null
        );
        when(monitoringServiceClient.getCurrentStatuses()).thenThrow(serverError);

        // When / Then
        webTestClient.get()
                .uri("/api/dashboard/statuses")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Monitoring Service Error")
                .jsonPath("$.status").isEqualTo(500);
    }

    @Test
    @DisplayName("Returns 503 when monitoring-service is unreachable")
    void monitoringServiceUnreachable_returnsProblemDetail503() {
        // Given
        WebClientRequestException connectionRefused = new WebClientRequestException(
                new ConnectException("Connection refused"),
                org.springframework.http.HttpMethod.GET,
                URI.create("http://localhost:9999/api/monitoring/read/current-statuses"),
                HttpHeaders.EMPTY
        );
        when(monitoringServiceClient.getCurrentStatuses()).thenThrow(connectionRefused);

        // When / Then
        webTestClient.get()
                .uri("/api/dashboard/statuses")
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectBody()
                .jsonPath("$.title").isEqualTo("Monitoring Service Unavailable")
                .jsonPath("$.status").isEqualTo(503)
                .jsonPath("$.detail").isEqualTo("Unable to connect to monitoring-service");
    }

}
