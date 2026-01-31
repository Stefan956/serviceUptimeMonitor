package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.service;

import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dao.MonitoredServiceRepository;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto.MonitoredServiceRequestDto;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto.MonitoredServiceResponseDto;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.model.MonitoredService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.github.Stefan956.serviceUptimeMonitor.monitoring_service.mapper.MonitoredServiceMapper.mapToMonitoredServiceResponseDto;


@Service
@RequiredArgsConstructor
@Transactional
public class MonitoredServiceManagementService {

    private final MonitoredServiceRepository serviceRepository;

    public MonitoredServiceResponseDto create(MonitoredServiceRequestDto request) {
        MonitoredService service = new MonitoredService();
        service.setName(request.name());
        service.setUrl(request.url());
        service.setCheckIntervalSeconds(request.checkIntervalSeconds());
        service.setEnabled(true);
        service.setCreatedAt(LocalDateTime.now());

        MonitoredService savedService = serviceRepository.save(service);
        return mapToMonitoredServiceResponseDto(savedService);
    }

    public MonitoredServiceResponseDto update(
            UUID serviceId,
            MonitoredServiceRequestDto request
    ) {
        MonitoredService service = findEntity(serviceId);
        service.setName(request.name());
        service.setUrl(request.url());
        service.setCheckIntervalSeconds(request.checkIntervalSeconds());
        service.setUpdatedAt(LocalDateTime.now());

        return mapToMonitoredServiceResponseDto(service);
    }

    public void enable(UUID serviceId) {
        MonitoredService service = findEntity(serviceId);
        service.setEnabled(true);
    }

    public void disable(UUID serviceId) {
        MonitoredService service = findEntity(serviceId);
        service.setEnabled(false);
    }

    @Transactional(readOnly = true)
    public List<MonitoredServiceResponseDto> getAll() {
        return serviceRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MonitoredServiceResponseDto getById(UUID serviceId) {
        return toResponse(findEntity(serviceId));
    }


    // Helper methods
    private MonitoredService findEntity(UUID serviceId) {
        return serviceRepository.findById(serviceId).
                orElseThrow(() ->
                        new EntityNotFoundException("Service not found: " + serviceId));
    }


    private MonitoredServiceResponseDto toResponse(MonitoredService service) {
        return new MonitoredServiceResponseDto(
                service.getId(),
                service.getName(),
                service.getUrl(),
                service.getCheckIntervalSeconds(),
                service.isEnabled(),
                service.getCreatedAt(),
                service.getUpdatedAt()
        );
    }

}