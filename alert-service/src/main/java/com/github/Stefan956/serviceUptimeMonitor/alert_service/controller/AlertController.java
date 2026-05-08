package com.github.Stefan956.serviceUptimeMonitor.alert_service.controller;

import com.github.Stefan956.serviceUptimeMonitor.alert_service.dto.AlertRequestDto;
import com.github.Stefan956.serviceUptimeMonitor.alert_service.dto.AlertResponseDto;
import com.github.Stefan956.serviceUptimeMonitor.alert_service.service.AlertProcessorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
@Tag(name = "Alerts", description = "Inbound alert processing and alert history retrieval")
public class AlertController {

    private final AlertProcessorService alertProcessorService;


    @Operation(
            summary = "Process a service status-change event",
            description = "Called internally by the Monitoring Service whenever a monitored service transitions " +
                    "between UP and DOWN states. The Alert Service validates the payload, persists the alert record, " +
                    "and dispatches a notification via the configured channel (e-mail, Slack, or webhook)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Alert accepted and queued for processing"),
            @ApiResponse(responseCode = "400", description = "Invalid or incomplete request body",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/status-change")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void processStatusChange(@Valid @RequestBody AlertRequestDto request) {
        alertProcessorService.processStatusChange(request);
    }


    @Operation(
            summary = "Get all alerts",
            description = "Returns the complete alert history across all monitored services, ordered by notification time descending."
    )
    @ApiResponse(responseCode = "200", description = "Full alert history")
    @GetMapping
    public List<AlertResponseDto> getAllAlerts() {
        return alertProcessorService.getAllAlerts();
    }


    @Operation(
            summary = "Get alerts for a specific service",
            description = "Filters the alert history to only include events related to the given service name. " +
                    "The match is case-sensitive and must be an exact service name."
    )
    @ApiResponse(responseCode = "200", description = "Alert history for the specified service — empty array when no alerts exist")
    @GetMapping("/service/{serviceName}")
    public List<AlertResponseDto> getAlertsByServiceName(
            @Parameter(description = "Exact name of the monitored service", example = "Payment Service", required = true)
            @PathVariable String serviceName) {
        return alertProcessorService.getAlertsByServiceName(serviceName);
    }
}
