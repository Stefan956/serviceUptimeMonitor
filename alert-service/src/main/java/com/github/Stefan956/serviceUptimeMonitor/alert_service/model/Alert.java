package com.github.Stefan956.serviceUptimeMonitor.alert_service.model;

import com.github.Stefan956.serviceUptimeMonitor.alert_service.enums.NotificationChannel;
import com.github.Stefan956.serviceUptimeMonitor.alert_service.enums.ServiceHealthStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "alerts")
@Data
@NoArgsConstructor
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String serviceName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ServiceHealthStatus oldStatus;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ServiceHealthStatus newStatus;

    private int httpStatusCode;

    @Column(nullable = false)
    private LocalDateTime changedAt;

    @Column(nullable = false)
    private LocalDateTime notifiedAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationChannel notificationChannel;

    @PrePersist
    void prePersist() {
        if (notifiedAt == null) {
            notifiedAt = LocalDateTime.now();
        }
    }
}
