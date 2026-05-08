package com.github.Stefan956.serviceUptimeMonitor.dashboard_service.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ProblemDetail;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GlobalExceptionHandler Unit Tests")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("handleWebClientResponseException returns ProblemDetail with upstream status code")
    void handleWebClientResponseException_returnsProblemDetailWithUpstreamStatus() {
        // Given
        WebClientResponseException ex = WebClientResponseException.create(
                404,
                "Not Found",
                HttpHeaders.EMPTY,
                new byte[0],
                null
        );

        // When
        ProblemDetail problem = handler.handleWebClientResponseException(ex);

        // Then
        assertThat(problem.getStatus()).isEqualTo(404);
        assertThat(problem.getTitle()).isEqualTo("Monitoring Service Error");
        assertThat(problem.getDetail()).contains("Not Found");
    }

    @Test
    @DisplayName("handleWebClientRequestException returns 503 Service Unavailable")
    void handleWebClientRequestException_returns503() {
        // Given
        WebClientRequestException ex = new WebClientRequestException(
                new java.net.ConnectException("Connection refused"),
                org.springframework.http.HttpMethod.GET,
                URI.create("http://localhost:8080/api/monitoring/read/current-statuses"),
                HttpHeaders.EMPTY
        );

        // When
        ProblemDetail problem = handler.handleWebClientRequestException(ex);

        // Then
        assertThat(problem.getStatus()).isEqualTo(503);
        assertThat(problem.getTitle()).isEqualTo("Monitoring Service Unavailable");
        assertThat(problem.getDetail()).isEqualTo("Unable to connect to monitoring-service");
    }
}
