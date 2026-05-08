package com.github.Stefan956.serviceUptimeMonitor.alert_service.exception;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GlobalExceptionHandler Unit Tests")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Should return 404 ProblemDetail for EntityNotFoundException")
    void handleEntityNotFound_shouldReturn404ProblemDetail() {
        // Given
        EntityNotFoundException ex = new EntityNotFoundException("Alert not found: abc-123");

        // When
        ProblemDetail result = handler.handleEntityNotFound(ex);

        // Then
        assertThat(result.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(result.getTitle()).isEqualTo("Entity Not Found");
        assertThat(result.getDetail()).isEqualTo("Alert not found: abc-123");
    }

    @Test
    @DisplayName("Should return 500 ProblemDetail for AlertProcessingException")
    void handleAlertProcessingException_shouldReturn500ProblemDetail() {
        // Given
        AlertProcessingException ex = new AlertProcessingException("Failed to process alert");

        // When
        ProblemDetail result = handler.handleAlertProcessingException(ex);

        // Then
        assertThat(result.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(result.getTitle()).isEqualTo("Alert Processing Error");
        assertThat(result.getDetail()).isEqualTo("Failed to process alert");
    }
}
