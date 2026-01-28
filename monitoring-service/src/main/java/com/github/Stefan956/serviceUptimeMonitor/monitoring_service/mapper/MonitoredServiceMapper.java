package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.mapper;

import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto.MonitoredServiceRequestDto;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto.MonitoredServiceResponseDto;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.model.MonitoredService;

public class MonitoredServiceMapper {

    public static MonitoredServiceResponseDto mapToMonitoredServiceResponseDto
            (MonitoredService monitoredService) {
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

    public static MonitoredService mapToMonitoredServiceEntity
            (MonitoredServiceResponseDto monitoredServiceResponseDto,
             MonitoredService monitoredService) {
        monitoredService = new MonitoredService();
        monitoredService.setId(monitoredServiceResponseDto.id());
        monitoredService.setName(monitoredServiceResponseDto.name());
        monitoredService.setUrl(monitoredServiceResponseDto.url());
        monitoredService.setCheckIntervalSeconds(monitoredServiceResponseDto.checkIntervalSeconds());
        monitoredService.setEnabled(monitoredServiceResponseDto.enabled());
        monitoredService.setCreatedAt(monitoredServiceResponseDto.createdAt());
        monitoredService.setUpdatedAt(monitoredServiceResponseDto.updatedAt());
        return monitoredService;
    }

    public MonitoredService fromCreateRequest(MonitoredServiceRequestDto dto) {
        MonitoredService service = new MonitoredService();
        service.setName(dto.name());
        service.setUrl(dto.url());
        service.setCheckIntervalSeconds(dto.checkIntervalSeconds());
        service.setEnabled(true);
        return service;
    }


    public void updateEntity(
            MonitoredService service,
            MonitoredServiceRequestDto dto
    ) {
        service.setName(dto.name());
        service.setUrl(dto.url());
        service.setCheckIntervalSeconds(dto.checkIntervalSeconds());
    }

}
