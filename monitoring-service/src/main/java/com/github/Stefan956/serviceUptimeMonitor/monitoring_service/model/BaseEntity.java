package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

@MappedSuperclass
@Getter @Setter @ToString
public class BaseEntity {

    @Id
    @GeneratedValue
    @Column(name="service_id", updatable = false, nullable = false)
    private UUID id;

    @Column(name="created_at" ,updatable = false)
    private LocalDateTime createdAt;

    @Column(name="updated_at" ,insertable = false)
    private LocalDateTime updatedAt;
}
