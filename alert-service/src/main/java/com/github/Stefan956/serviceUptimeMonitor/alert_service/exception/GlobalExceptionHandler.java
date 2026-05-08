package com.github.Stefan956.serviceUptimeMonitor.alert_service.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleEntityNotFound(EntityNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, ex.getMessage()
        );
        problemDetail.setTitle("Entity Not Found");
        return problemDetail;
    }

    @ExceptionHandler(AlertProcessingException.class)
    public ProblemDetail handleAlertProcessingException(AlertProcessingException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage()
        );
        problemDetail.setTitle("Alert Processing Error");
        return problemDetail;
    }
}
