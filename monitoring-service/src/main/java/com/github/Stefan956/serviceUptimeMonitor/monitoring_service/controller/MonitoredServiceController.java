package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.controller;

import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto.MonitoredServiceRequestDto;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto.MonitoredServiceResponseDto;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto.ServiceStatusSummaryDto;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.service.MonitoredServiceManagementService;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.service.MonitoringReadService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

//TODO: validate input; call service layer; return http responses

@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
public class MonitoredServiceController {

    private final MonitoredServiceManagementService managementService;


    // MANAGEMENT ENDPOINTS (ADMIN)

    @PostMapping("/services")
    @ResponseStatus(HttpStatus.CREATED)
    public MonitoredServiceResponseDto create(@RequestBody MonitoredServiceRequestDto request) {
        return managementService.create(request);
    }

    @PutMapping("/services/{id}")
    public MonitoredServiceResponseDto update(
            @PathVariable UUID id,
            @RequestBody MonitoredServiceRequestDto request
    ) {
        return managementService.update(id, request);
    }


    @PatchMapping("services/{id}/enable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void enable(@PathVariable UUID id) {
        managementService.enable(id);
    }

    @PatchMapping("services/{id}/disable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disable(@PathVariable UUID id) {
        managementService.disable(id);
    }

    @GetMapping("/services/{id}")
    public MonitoredServiceResponseDto getById(@PathVariable UUID id) {
        return managementService.getById(id);
    }

    @GetMapping("/services")
    public List<MonitoredServiceResponseDto> getAll() {
        return managementService.getAll();
    }
}