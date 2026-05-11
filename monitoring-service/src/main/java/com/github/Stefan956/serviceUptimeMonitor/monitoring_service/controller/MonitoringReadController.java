package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.controller;

import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto.ServiceStatusHistoryDto;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto.ServiceStatusSummaryDto;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.service.MonitoringReadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/monitoring/read")
@RequiredArgsConstructor
@Tag(name = "Monitoring Read", description = "Read-only endpoints consumed by the Dashboard Service, Alert Service, and Grafana")
public class MonitoringReadController {

    private final MonitoringReadService monitoringReadService;

    @Operation(
            summary = "Get current status of all services",
            description = "Returns the most recent health-check result for every enabled service. " +
                    "Consumed by the Dashboard Service and Grafana for live status displays."
    )
    @ApiResponse(responseCode = "200", description = "Current status snapshot for all monitored services")
    @GetMapping("/current-statuses")
    public List<ServiceStatusSummaryDto> currentStatuses() {
        return monitoringReadService.getCurrentStatuses();
    }


    @Operation(
            summary = "Get full check history for a service",
            description = "Returns the complete ordered history of health-check results for the specified service. " +
                    "Each entry represents one scheduler execution. " +
                    "Tip: consider adding pagination for services with long histories."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Full check history for the specified service"),
            @ApiResponse(responseCode = "404", description = "Service not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/history/{serviceId}")
    public List<ServiceStatusHistoryDto> getServiceHistoryById(
            @Parameter(description = "UUID of the monitored service", required = true)
            @PathVariable UUID serviceId) {
        return monitoringReadService.getHistory(serviceId);
    }
}
