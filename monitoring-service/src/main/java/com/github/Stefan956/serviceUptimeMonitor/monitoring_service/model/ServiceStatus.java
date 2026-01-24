// Records what happened during the Monitoring
package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter @Setter @ToString @AllArgsConstructor @NoArgsConstructor
public class ServiceStatus extends BaseEntity {

    @ManyToOne
    @JoinColumn(name="monitored_service_id", nullable = false)
    private MonitoredService monitoredService;

    @Enumerated(EnumType.STRING)
    @Column(name="status", nullable = false)
    private ServiceHealthStatus status;

    @Column(name="http_status_code", nullable = false)
    private int httpStatusCode;

    @Column(name="response_time")
    private long responseTimeMs;

    @Column(name="checked_at", nullable = false)
    private LocalDateTime checkedAt;
}