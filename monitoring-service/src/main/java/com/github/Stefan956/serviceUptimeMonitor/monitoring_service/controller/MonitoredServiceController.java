package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.controller;

import org.springframework.web.bind.annotation.*;

//TODO: validate input; call service layer; return http responses

@RestController
@RequestMapping("/api/monitoring")
public class MonitoredServiceController {
    //    @Autowired
//    private MonitorService monitorService;
//
//    @PostMapping("/services")
//    public ResponseEntity<MonitorService> createService(@RequestBody MonitorService service) {
//        MonitorService createdService = monitorService.createService(service);
//        return new ResponseEntity<>(createdService, HttpStatus.CREATED);
//    }
//
//    @GetMapping("/services/{id}")
//    public ResponseEntity<MonitorService> getService(@PathVariable Long id) {
//        MonitorService service = monitorService.getServiceById(id);
//        return new ResponseEntity<>(service, HttpStatus.OK);
//    }
//
//    // A method to list all services could be added here
//
//    @PutMapping("/services/{id}")
//    public ResponseEntity<MonitorService> updateService(@PathVariable Long id, @RequestBody MonitorService service) {
//        MonitorService updatedService = monitorService.updateService(id, service);
//        return new ResponseEntity<>(updatedService, HttpStatus.OK);
//    }
//
//    @DeleteMapping("/services/{id}")
//    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
//        monitorService.deleteService(id);
//        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
//    }
}