package com.github.Stefan956.serviceUptimeMonitor.alert_service.repository;

import com.github.Stefan956.serviceUptimeMonitor.alert_service.dao.AlertRepository;
import com.github.Stefan956.serviceUptimeMonitor.alert_service.model.Alert;
import com.github.Stefan956.serviceUptimeMonitor.alert_service.model.NotificationChannel;
import com.github.Stefan956.serviceUptimeMonitor.alert_service.model.ServiceHealthStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("AlertRepository Integration Tests")
class AlertRepositoryIntegrationTest {

    @Autowired
    private AlertRepository alertRepository;

    @BeforeEach
    void setUp() {
        alertRepository.deleteAll();
    }

    @Test
    @DisplayName("save persists an Alert with generated ID")
    void save_persistsAlertWithGeneratedId() {
        // Given
        Alert alert = new Alert();
        alert.setServiceName("test-service");
        alert.setOldStatus(ServiceHealthStatus.UP);
        alert.setNewStatus(ServiceHealthStatus.DOWN);
        alert.setHttpStatusCode(503);
        alert.setChangedAt(LocalDateTime.now());
        alert.setNotifiedAt(LocalDateTime.now());
        alert.setNotificationChannel(NotificationChannel.CONSOLE);

        // When
        Alert saved = alertRepository.save(alert);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getServiceName()).isEqualTo("test-service");
        assertThat(saved.getNewStatus()).isEqualTo(ServiceHealthStatus.DOWN);
    }

    @Test
    @DisplayName("findAllByOrderByNotifiedAtDesc returns alerts ordered by notifiedAt descending")
    void findAllByOrderByNotifiedAtDesc_returnsOrderedResults() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        saveAlert("service-a", now.minusMinutes(10));
        saveAlert("service-b", now.minusMinutes(5));
        saveAlert("service-c", now);

        // When
        List<Alert> result = alertRepository.findAllByOrderByNotifiedAtDesc(Pageable.unpaged()).getContent();

        // Then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getNotifiedAt()).isAfter(result.get(1).getNotifiedAt());
        assertThat(result.get(1).getNotifiedAt()).isAfter(result.get(2).getNotifiedAt());
    }

    @Test
    @DisplayName("findAllByOrderByNotifiedAtDesc returns empty page when no alerts exist")
    void findAllByOrderByNotifiedAtDesc_returnsEmptyWhenNoAlerts() {
        // When
        Page<Alert> result = alertRepository.findAllByOrderByNotifiedAtDesc(Pageable.unpaged());

        // Then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("findByServiceNameOrderByNotifiedAtDesc returns filtered and ordered results")
    void findByServiceNameOrderByNotifiedAtDesc_returnsFilteredAndOrdered() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        saveAlert("payment-service", now.minusMinutes(10));
        saveAlert("payment-service", now);
        saveAlert("auth-service", now.minusMinutes(5));

        // When
        List<Alert> result = alertRepository.findByServiceNameOrderByNotifiedAtDesc("payment-service");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(a -> a.getServiceName().equals("payment-service"));
        assertThat(result.get(0).getNotifiedAt()).isAfter(result.get(1).getNotifiedAt());
    }

    @Test
    @DisplayName("findByServiceNameOrderByNotifiedAtDesc returns empty for unknown service")
    void findByServiceNameOrderByNotifiedAtDesc_returnsEmptyForUnknownService() {
        // Given
        saveAlert("payment-service", LocalDateTime.now());

        // When
        List<Alert> result = alertRepository.findByServiceNameOrderByNotifiedAtDesc("unknown-service");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findTopByServiceNameAndNewStatusAndNotificationChannelOrderByNotifiedAtDesc returns the latest matching alert")
    void findTopByServiceNameAndNewStatusAndNotificationChannelOrderByNotifiedAtDesc_returnsLatestMatch() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        saveAlert("payment-service", now.minusMinutes(10));
        Alert latest = saveAlert("payment-service", now);
        saveAlert("auth-service", now.minusMinutes(5));

        // When
        Optional<Alert> result = alertRepository
                .findTopByServiceNameAndNewStatusAndNotificationChannelOrderByNotifiedAtDesc(
                        "payment-service", ServiceHealthStatus.DOWN, NotificationChannel.CONSOLE);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(latest.getId());
    }

    @Test
    @DisplayName("findTopByServiceNameAndNewStatusAndNotificationChannelOrderByNotifiedAtDesc returns empty when no match")
    void findTopByServiceNameAndNewStatusAndNotificationChannelOrderByNotifiedAtDesc_returnsEmptyWhenNoMatch() {
        // Given
        saveAlert("payment-service", LocalDateTime.now());

        // When
        Optional<Alert> result = alertRepository
                .findTopByServiceNameAndNewStatusAndNotificationChannelOrderByNotifiedAtDesc(
                        "payment-service", ServiceHealthStatus.UP, NotificationChannel.CONSOLE);

        // Then
        assertThat(result).isEmpty();
    }

    // Helper method
    private Alert saveAlert(String serviceName, LocalDateTime notifiedAt) {
        Alert alert = new Alert();
        alert.setServiceName(serviceName);
        alert.setOldStatus(ServiceHealthStatus.UP);
        alert.setNewStatus(ServiceHealthStatus.DOWN);
        alert.setHttpStatusCode(503);
        alert.setChangedAt(LocalDateTime.now());
        alert.setNotifiedAt(notifiedAt);
        alert.setNotificationChannel(NotificationChannel.CONSOLE);
        return alertRepository.save(alert);
    }
}
