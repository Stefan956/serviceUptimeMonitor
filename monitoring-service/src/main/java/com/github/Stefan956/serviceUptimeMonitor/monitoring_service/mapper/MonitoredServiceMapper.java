package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.mapper;

import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto.MonitoredServiceRequestDto;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto.MonitoredServiceResponseDto;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.model.MonitoredService;
import org.springframework.stereotype.Component;

@Component
public class MonitoredServiceMapper {

    public MonitoredServiceResponseDto toResponseDto(MonitoredService monitoredService) {
        return new MonitoredServiceResponseDto(
                monitoredService.getId(),
                monitoredService.getName(),
                monitoredService.getUrl(),
                monitoredService.getCheckIntervalSeconds(),
                monitoredService.isEnabled(),
                monitoredService.getCreatedAt(),
                monitoredService.getUpdatedAt()
        );
    }

    public MonitoredService fromCreateRequest(MonitoredServiceRequestDto dto) {
        MonitoredService service = new MonitoredService();
        service.setName(dto.name());
        service.setUrl(dto.url());
        service.setCheckIntervalSeconds(dto.checkIntervalSeconds());
        service.setEnabled(true);
        return service;
    }

    public void updateEntity(MonitoredService service, MonitoredServiceRequestDto dto) {
        service.setName(dto.name());
        service.setUrl(dto.url());
        service.setCheckIntervalSeconds(dto.checkIntervalSeconds());
    }
}
