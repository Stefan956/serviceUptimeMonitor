package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.service;

import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.persistence.repository.MonitoredServiceRepository;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.requests.request.MonitoredServiceRequestDto;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.requests.response.MonitoredServiceResponseDto;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.mapper.MonitoredServiceMapper;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.persistence.entity.MonitoredService;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.util.Constants;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MonitoredServiceManagementService {

    private final MonitoredServiceRepository serviceRepository;
    private final MonitoredServiceMapper mapper;

    public MonitoredServiceResponseDto create(MonitoredServiceRequestDto request) {
        MonitoredService service = mapper.fromCreateRequest(request);
        MonitoredService savedService = serviceRepository.save(service);
        return mapper.toResponseDto(savedService);
    }

    public MonitoredServiceResponseDto update(UUID serviceId, MonitoredServiceRequestDto request) {
        MonitoredService service = findEntity(serviceId);
        mapper.updateEntity(service, request);
        service.setLastCheckedAt(null);

        return mapper.toResponseDto(service);
    }

    public void enable(UUID serviceId) {
        findEntity(serviceId).setEnabled(true);
    }

    public void disable(UUID serviceId) {
        findEntity(serviceId).setEnabled(false);
    }

    public void delete(UUID serviceId) {
        serviceRepository.delete(findEntity(serviceId));
    }

    @Transactional(readOnly = true)
    public Page<MonitoredServiceResponseDto> getAll(Pageable pageable) {
        return serviceRepository.findAll(pageable)
                .map(mapper::toResponseDto);
    }

    @Transactional(readOnly = true)
    public MonitoredServiceResponseDto getById(UUID serviceId) {
        return mapper.toResponseDto(findEntity(serviceId));
    }

    private MonitoredService findEntity(UUID serviceId) {
        return serviceRepository.findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException(Constants.SERVICE_NOT_FOUND_PREFIX + serviceId));
    }
}
