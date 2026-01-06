package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter @Setter @ToString
public class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="service_id", updatable = false, nullable = false)
    private Long id;

    @Column(name="created_at" ,updatable = false)
    private LocalDateTime createdAt;

    @Column(name="updated_at" ,insertable = false)
    private LocalDateTime updatedAt;
}
