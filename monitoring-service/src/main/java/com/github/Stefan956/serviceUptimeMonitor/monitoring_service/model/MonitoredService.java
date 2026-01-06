//TODO: id, serviceId, status, responseTime, checkedAt
package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Time;
import java.time.LocalDateTime;

@Entity
@Getter @Setter @ToString @AllArgsConstructor @NoArgsConstructor
public class MonitoredService extends BaseEntity {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="service_id", updatable = false, nullable = false)
    private Long serviceId;

    @Column(name="status", nullable = false)
    private String status;

    @Column(name="response_time")
    private Time responseTime;

    @Column(name="checked_at", nullable = false)
    private LocalDateTime checkedAt;
}