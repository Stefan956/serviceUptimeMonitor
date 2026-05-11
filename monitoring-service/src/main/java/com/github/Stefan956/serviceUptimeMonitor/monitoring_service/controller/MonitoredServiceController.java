package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.controller;

import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto.MonitoredServiceRequestDto;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto.MonitoredServiceResponseDto;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.service.MonitoredServiceManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
@Tag(name = "Monitored Services Management", description = "CRUD operations for registering and configuring services to monitor")
public class MonitoredServiceController {

    private final MonitoredServiceManagementService managementService;


    @Operation(
            summary = "Register a new service",
            description = "Registers a new service for periodic health-check monitoring. " +
                    "The service will be enabled immediately and scheduled for its first check within the next check cycle."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Service registered successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed — field errors are listed in the `errors` property of the response body",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/services")
    @ResponseStatus(HttpStatus.CREATED)
    public MonitoredServiceResponseDto create(@Valid @RequestBody MonitoredServiceRequestDto request) {
        return managementService.create(request);
    }


    @Operation(
            summary = "Update a registered service",
            description = "Replaces the name, URL, and check interval of an existing service. " +
                    "The enabled/disabled state is preserved and must be changed via the dedicated enable/disable endpoints."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Service updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed — field errors are listed in the `errors` property of the response body",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Service not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PutMapping("/services/{id}")
    public MonitoredServiceResponseDto update(
            @Parameter(description = "UUID of the service to update", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody MonitoredServiceRequestDto request
    ) {
        return managementService.update(id, request);
    }


    @Operation(
            summary = "Enable a service",
            description = "Re-enables a previously disabled service so it is included in the next scheduled health-check cycle."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Service enabled"),
            @ApiResponse(responseCode = "404", description = "Service not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PatchMapping("services/{id}/enable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void enable(
            @Parameter(description = "UUID of the service to enable", required = true)
            @PathVariable UUID id) {
        managementService.enable(id);
    }


    @Operation(
            summary = "Disable a service",
            description = "Pauses health checks for a service without deleting it. " +
                    "The service and its history are retained and checks resume when the service is re-enabled."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Service disabled"),
            @ApiResponse(responseCode = "404", description = "Service not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PatchMapping("services/{id}/disable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disable(
            @Parameter(description = "UUID of the service to disable", required = true)
            @PathVariable UUID id) {
        managementService.disable(id);
    }


    @Operation(
            summary = "Delete a service",
            description = "Permanently removes a service and all of its check history from the system. " +
                    "This action is irreversible — use disable instead if you want to pause checks temporarily."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Service deleted"),
            @ApiResponse(responseCode = "404", description = "Service not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping("/services/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Parameter(description = "UUID of the service to delete", required = true)
            @PathVariable UUID id) {
        managementService.delete(id);
    }


    @Operation(
            summary = "Get a service by ID",
            description = "Returns metadata for a single registered service including its current enabled state and check interval."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Service found"),
            @ApiResponse(responseCode = "404", description = "Service not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/services/{id}")
    public MonitoredServiceResponseDto getById(
            @Parameter(description = "UUID of the service to retrieve", required = true)
            @PathVariable UUID id) {
        return managementService.getById(id);
    }


    @Operation(
            summary = "List all registered services",
            description = "Returns a paginated list of all services registered in the system, both enabled and disabled."
    )
    @ApiResponse(responseCode = "200", description = "Paginated list of registered services")
    @GetMapping("/services")
    public Page<MonitoredServiceResponseDto> getAll(
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return managementService.getAll(pageable);
    }
}
