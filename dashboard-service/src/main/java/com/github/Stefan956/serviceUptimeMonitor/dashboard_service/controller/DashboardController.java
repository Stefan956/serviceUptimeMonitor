package com.github.Stefan956.serviceUptimeMonitor.dashboard_service.controller;

import com.github.Stefan956.serviceUptimeMonitor.dashboard_service.dto.DashboardOverviewDto;
import com.github.Stefan956.serviceUptimeMonitor.dashboard_service.dto.MonitoredServiceDto;
import com.github.Stefan956.serviceUptimeMonitor.dashboard_service.dto.ServiceStatusHistoryDto;
import com.github.Stefan956.serviceUptimeMonitor.dashboard_service.dto.ServiceStatusSummaryDto;
import com.github.Stefan956.serviceUptimeMonitor.dashboard_service.service.DashboardService;
import com.github.Stefan956.serviceUptimeMonitor.dashboard_service.service.DashboardSseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Aggregated status views and real-time streaming for the frontend dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final DashboardSseService dashboardSseService;


    @Operation(
            summary = "Get dashboard overview",
            description = "Returns an aggregated snapshot containing the total number of monitored services " +
                    "and a breakdown of how many are currently UP vs DOWN, together with the full status list."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Aggregated overview retrieved successfully"),
            @ApiResponse(responseCode = "503", description = "Monitoring Service is unreachable",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/overview")
    public DashboardOverviewDto getOverview() {
        return dashboardService.getOverview();
    }


    @Operation(
            summary = "Get current status of all services",
            description = "Returns the latest health-check result for every monitored service as reported by the Monitoring Service."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Current status list retrieved"),
            @ApiResponse(responseCode = "503", description = "Monitoring Service is unreachable",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/statuses")
    public List<ServiceStatusSummaryDto> getCurrentStatuses() {
        return dashboardService.getCurrentStatuses();
    }


    @Operation(
            summary = "List all registered services",
            description = "Proxies the Monitoring Service's service-list endpoint to provide frontend access " +
                    "to service metadata (name, URL, check interval, enabled flag)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Service list retrieved"),
            @ApiResponse(responseCode = "503", description = "Monitoring Service is unreachable",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/services")
    public List<MonitoredServiceDto> getAllServices() {
        return dashboardService.getAllMonitoredServices();
    }


    @Operation(
            summary = "Get check history for a service",
            description = "Proxies the Monitoring Service's history endpoint to expose the full ordered check history " +
                    "for a single service. Useful for timeline charts in the dashboard."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Service history retrieved"),
            @ApiResponse(responseCode = "404", description = "Service not found in Monitoring Service",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "503", description = "Monitoring Service is unreachable",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/history/{serviceId}")
    public List<ServiceStatusHistoryDto> getServiceHistory(
            @Parameter(description = "UUID of the monitored service", required = true)
            @PathVariable UUID serviceId) {
        return dashboardService.getServiceHistory(serviceId);
    }


    @Operation(
            summary = "Subscribe to real-time status updates (SSE)",
            description = "Opens a long-lived Server-Sent Events connection. The server pushes the current " +
                    "status of all monitored services every 30 seconds (configurable via `dashboard.polling.interval-ms`). " +
                    "The connection remains open until the client disconnects. " +
                    "Each event payload is a JSON array of `ServiceStatusSummaryDto`."
    )
    @ApiResponse(responseCode = "200", description = "SSE stream opened — events emitted every ~30 seconds")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamStatusUpdates() {
        return dashboardSseService.subscribe();
    }
}
