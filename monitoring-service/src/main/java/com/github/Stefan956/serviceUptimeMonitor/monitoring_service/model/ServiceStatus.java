//TODO: id, name, url, interval, enabled, lastChecked, status, responseTime, createdAt, updatedAt
package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;

@Entity
@Getter @Setter @ToString @AllArgsConstructor @NoArgsConstructor
public class ServiceStatus extends BaseEntity {

    @Column(name="name", nullable = false)
    private String name;

}