package com.github.Stefan956.serviceUptimeMonitor.alert_service.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "alert.email.enabled", havingValue = "true")
public class MailConfig {
    // Spring Boot auto-configures JavaMailSender from spring.mail.* properties.
    // This class serves as a marker to conditionally enable mail configuration.
}
