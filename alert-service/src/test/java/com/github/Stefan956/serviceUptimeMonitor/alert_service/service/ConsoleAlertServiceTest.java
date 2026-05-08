package com.github.Stefan956.serviceUptimeMonitor.alert_service.service;

import com.github.Stefan956.serviceUptimeMonitor.alert_service.model.Alert;
import com.github.Stefan956.serviceUptimeMonitor.alert_service.model.NotificationChannel;
import com.github.Stefan956.serviceUptimeMonitor.alert_service.model.ServiceHealthStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("ConsoleAlertService Unit Tests")
class ConsoleAlertServiceTest {

    private final ConsoleAlertService consoleAlertService = new ConsoleAlertService();

    @Test
    @DisplayName("Should log alert without throwing exception")
    void notify_shouldNotThrowException() {
        // Given
        Alert alert = createAlert("test-service");

        // When / Then
        assertThatCode(() -> consoleAlertService.notify(alert))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should return CONSOLE notification channel")
    void getChannel_shouldReturnConsole() {
        // When
        NotificationChannel channel = consoleAlertService.getChannel();

        // Then
        assertThat(channel).isEqualTo(NotificationChannel.CONSOLE);
    }

    private Alert createAlert(String serviceName) {
        Alert alert = new Alert();
        alert.setServiceName(serviceName);
        alert.setOldStatus(ServiceHealthStatus.UP);
        alert.setNewStatus(ServiceHealthStatus.DOWN);
        alert.setHttpStatusCode(503);
        alert.setChangedAt(LocalDateTime.now());
        alert.setNotifiedAt(LocalDateTime.now());
        alert.setNotificationChannel(NotificationChannel.CONSOLE);
        return alert;
    }
}
