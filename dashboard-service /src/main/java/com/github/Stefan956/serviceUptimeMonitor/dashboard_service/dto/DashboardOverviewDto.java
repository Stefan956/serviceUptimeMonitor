package com.github.Stefan956.serviceUptimeMonitor.dashboard_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Aggregated overview of all monitored services returned by the dashboard overview endpoint")
public record DashboardOverviewDto(

        @Schema(description = "Total number of registered monitored services", example = "12")
        int totalServices,

        @Schema(description = "Number of services currently in UP state", example = "10")
        int servicesUp,

        @Schema(description = "Number of services currently in DOWN state", example = "2")
        int servicesDown,

        @Schema(description = "Full list of the latest health-check result per service")
        List<ServiceStatusSummaryDto> statuses
) {}
