//Defines what is monitored
package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "url"))
@Getter @Setter @ToString @AllArgsConstructor @NoArgsConstructor
public class MonitoredService extends BaseEntity {

    @Column(name="name", nullable = false)
    private String name;

    @Column(name="url", nullable = false)
    private String url;

    @Column(name="check_interval", nullable = false)
    private int checkIntervalSeconds;

    @Column(name="enabled", nullable = false)
    private boolean enabled;

    @Column(name="last_checked_at")
    private LocalDateTime lastCheckedAt;

    @ToString.Exclude
    @OneToMany(mappedBy = "monitoredService", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServiceStatus> serviceStatuses;
}