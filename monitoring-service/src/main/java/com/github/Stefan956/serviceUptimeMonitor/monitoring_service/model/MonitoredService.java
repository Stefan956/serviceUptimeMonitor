//Defines what is monitored
package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@SuppressWarnings("ALL")
@Entity
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

    @OneToMany(mappedBy = "monitoredService")
    private List<ServiceStatus> serviceStatuses;
}