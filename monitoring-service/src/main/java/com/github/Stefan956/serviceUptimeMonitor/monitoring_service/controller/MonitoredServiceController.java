package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.controller;

import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto.ServiceStatusSummaryDto;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.model.MonitoredService;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.service.MonitoringReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//TODO: validate input; call service layer; return http responses

@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
public class MonitoredServiceController {

//    private final MonitoredService monitoredService;
    private final MonitoringReadService monitoringReadService;

    @GetMapping("/current-status")
    public List<ServiceStatusSummaryDto> currentStatus() {
        return monitoringReadService.getCurrentStatuses();
    }


//    @PostMapping("/services")
//    public ResponseEntity<MonitoredService> createService(@RequestBody MonitoredService service) {
//        MonitoredService createdService = monitoredService.createService(service);
//        return new ResponseEntity<>(createdService, HttpStatus.CREATED);
//    }
//
//    @GetMapping("/services/{id}")
//    public ResponseEntity<MonitoredService> getService(@PathVariable Long id) {
//        MonitoredService service = monitoredService.getServiceById(id);
//        return new ResponseEntity<>(service, HttpStatus.OK);
//    }
//
//    // A method to list all services could be added here
//
//    @PutMapping("/services/{id}")
//    public ResponseEntity<MonitoredService> updateService(@PathVariable Long id, @RequestBody MonitoredService service) {
//        MonitoredService updatedService = monitoredService.updateService(id, service);
//        return new ResponseEntity<>(updatedService, HttpStatus.OK);
//    }
//
//    @DeleteMapping("/services/{id}")
//    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
//        monitoredService.deleteService(id);
//        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
//    }
}