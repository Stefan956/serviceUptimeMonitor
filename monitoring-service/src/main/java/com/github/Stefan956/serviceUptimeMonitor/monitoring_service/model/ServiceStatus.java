// Records what happened during the Monitoring
package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.model;

import jakarta.persistence.*;
import jakarta.transaction.Status;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter @Setter @ToString @AllArgsConstructor @NoArgsConstructor
public class ServiceStatus extends BaseEntity {

    @ManyToOne
    @JoinColumn(name="monitored_service_id", nullable = false)
    private MonitoredService monitoredService;

    @Column(name="status", nullable = false)
    private String status; //UP or DOWN

    @Column(name="http_status_code", nullable = false)
    private int httpStatusCode;

    @Column(name="response_time")
    private long responseTimeMs;

    @Column(name="checked_at", nullable = false)
    private LocalDateTime checkedAt;
}