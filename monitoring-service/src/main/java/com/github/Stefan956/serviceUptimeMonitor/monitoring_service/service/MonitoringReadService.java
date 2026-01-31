package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.service;

import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dao.MonitoringReadRepository;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dao.ServiceStatusRepository;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto.ServiceStatusHistoryDto;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto.ServiceStatusSummaryDto;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.model.ServiceStatus;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MonitoringReadService {

    private final ServiceStatusRepository serviceStatusRepository;
    private final MonitoringReadRepository readRepository;

    @Transactional(readOnly = true)
    public List<ServiceStatusSummaryDto> getCurrentStatuses() {
        return readRepository.findCurrentStatusPerService();
    }

    @Transactional(readOnly = true)
    public List<ServiceStatusHistoryDto> getHistory(UUID serviceId) {

        // Validation
        if (!serviceStatusRepository.existsById(serviceId)) {
            throw new EntityNotFoundException(
                    "Service not found: " + serviceId
            );
        }

        return serviceStatusRepository
                .findByMonitoredServiceIdOrderByCheckedAtDesc(serviceId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private ServiceStatusHistoryDto toDto(ServiceStatus status) {
        return new ServiceStatusHistoryDto(
                status.getId(),
                status.getStatus(),
                status.getHttpStatusCode(),
                status.getResponseTimeMs(),
                status.getCheckedAt()
        );
    }
}
