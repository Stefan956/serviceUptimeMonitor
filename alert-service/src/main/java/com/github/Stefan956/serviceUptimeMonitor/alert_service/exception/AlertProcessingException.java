package com.github.Stefan956.serviceUptimeMonitor.alert_service.exception;

public class AlertProcessingException extends RuntimeException{
    public AlertProcessingException(String message) {
        super(message);
    }

    public AlertProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
