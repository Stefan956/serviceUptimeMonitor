package com.github.Stefan956.serviceUptimeMonitor.alert_service.service;

import com.github.Stefan956.serviceUptimeMonitor.alert_service.model.Alert;
import com.github.Stefan956.serviceUptimeMonitor.alert_service.model.NotificationChannel;
import com.github.Stefan956.serviceUptimeMonitor.alert_service.model.ServiceHealthStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailAlertService Unit Tests")
class EmailAlertServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailAlertService emailAlertService;

    @BeforeEach
    void setUp() {
        emailAlertService = new EmailAlertService(mailSender);
        ReflectionTestUtils.setField(emailAlertService, "fromAddress", "alerts@test.com");
        ReflectionTestUtils.setField(emailAlertService, "toAddress", "admin@test.com");
    }

    @Test
    @DisplayName("Should send email with correct fields")
    void notify_shouldSendEmailWithCorrectFields() {
        // Given
        Alert alert = createAlert("payment-service", ServiceHealthStatus.UP, ServiceHealthStatus.DOWN, 503);

        // When
        emailAlertService.notify(alert);

        // Then
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getFrom()).isEqualTo("alerts@test.com");
        assertThat(sentMessage.getTo()).containsExactly("admin@test.com");
        assertThat(sentMessage.getSubject()).isEqualTo("Service Alert: payment-service is DOWN");
        assertThat(sentMessage.getText()).contains("payment-service");
        assertThat(sentMessage.getText()).contains("UP -> DOWN");
        assertThat(sentMessage.getText()).contains("503");
    }

    @Test
    @DisplayName("Should not propagate exception when mail sender fails")
    void notify_shouldNotThrowException_whenMailSenderFails() {
        // Given
        Alert alert = createAlert("test-service", ServiceHealthStatus.UP, ServiceHealthStatus.DOWN, 503);
        doThrow(new MailSendException("SMTP connection failed"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // When / Then
        assertThatCode(() -> emailAlertService.notify(alert))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should return EMAIL notification channel")
    void getChannel_shouldReturnEmail() {
        // When
        NotificationChannel channel = emailAlertService.getChannel();

        // Then
        assertThat(channel).isEqualTo(NotificationChannel.EMAIL);
    }

    private Alert createAlert(String serviceName, ServiceHealthStatus oldStatus,
                               ServiceHealthStatus newStatus, int httpStatusCode) {
        Alert alert = new Alert();
        alert.setServiceName(serviceName);
        alert.setOldStatus(oldStatus);
        alert.setNewStatus(newStatus);
        alert.setHttpStatusCode(httpStatusCode);
        alert.setChangedAt(LocalDateTime.now());
        alert.setNotifiedAt(LocalDateTime.now());
        alert.setNotificationChannel(NotificationChannel.CONSOLE);
        return alert;
    }
}
