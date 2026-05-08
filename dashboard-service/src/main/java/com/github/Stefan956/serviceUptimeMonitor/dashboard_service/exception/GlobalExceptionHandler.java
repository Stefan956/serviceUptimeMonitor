package com.github.Stefan956.serviceUptimeMonitor.dashboard_service.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebClientResponseException.class)
    public ProblemDetail handleWebClientResponseException(WebClientResponseException ex) {
        log.error("Monitoring-service responded with error: {} {}", ex.getStatusCode(), ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatus(ex.getStatusCode().value());
        problem.setTitle("Monitoring Service Error");
        problem.setDetail("Monitoring-service returned: " + ex.getStatusText());
        return problem;
    }

    @ExceptionHandler(WebClientRequestException.class)
    public ProblemDetail handleWebClientRequestException(WebClientRequestException ex) {
        log.error("Failed to connect to monitoring-service: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);
        problem.setTitle("Monitoring Service Unavailable");
        problem.setDetail("Unable to connect to monitoring-service");
        return problem;
    }
}
