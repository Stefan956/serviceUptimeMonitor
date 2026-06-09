package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.service;

import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.persistence.repository.MonitoredServiceRepository;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.persistence.repository.MonitoringReadRepository;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.persistence.repository.ServiceStatusRepository;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.requests.response.ServiceStatusHistoryDto;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.requests.response.ServiceStatusSummaryDto;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.persistence.entity.ServiceStatus;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.util.Constants;
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

    private final MonitoredServiceRepository monitoredServiceRepository;
    private final ServiceStatusRepository serviceStatusRepository;
    private final MonitoringReadRepository readRepository;

    public List<ServiceStatusSummaryDto> getCurrentStatuses() {
        return readRepository.findCurrentStatusPerService();
    }

    public List<ServiceStatusHistoryDto> getHistory(UUID serviceId) {

        // Validation
        if (!monitoredServiceRepository.existsById(serviceId)) {
            throw new EntityNotFoundException(
                    Constants.SERVICE_NOT_FOUND_PREFIX + serviceId
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
