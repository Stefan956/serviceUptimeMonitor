package com.github.Stefan956.serviceUptimeMonitor.alert_service.service;

import com.github.Stefan956.serviceUptimeMonitor.alert_service.model.Alert;
import com.github.Stefan956.serviceUptimeMonitor.alert_service.model.NotificationChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "alert.email.enabled", havingValue = "true")
public class EmailAlertService implements NotificationService {

    private final JavaMailSender mailSender;

    @Value("${alert.email.from}")
    private String fromAddress;

    @Value("${alert.email.to}")
    private String toAddress;

    @Override
    public void notify(Alert alert) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(toAddress);
            message.setSubject("Service Alert: " + alert.getServiceName() + " is " + alert.getNewStatus());
            message.setText(String.format(
                    "Service: %s%nStatus changed: %s -> %s%nHTTP Status Code: %d%nChanged at: %s",
                    alert.getServiceName(),
                    alert.getOldStatus(),
                    alert.getNewStatus(),
                    alert.getHttpStatusCode(),
                    alert.getChangedAt()
            ));
            mailSender.send(message);
            log.info("Email alert sent for service '{}'", alert.getServiceName());
        } catch (Exception e) {
            log.error("Failed to send email alert for service '{}': {}", alert.getServiceName(), e.getMessage());
        }
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }
}
