// Records what happened during the Monitoring
package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "service_status",
       indexes = @Index(name = "idx_service_status_service_id_checked_at",
                        columnList = "monitored_service_id, checked_at"))
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