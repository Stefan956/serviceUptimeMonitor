package com.github.Stefan956.serviceUptimeMonitor.dashboard_service;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@OpenAPIDefinition(
		info = @Info(
				title = "Dashboard Service API",
				version = "1.0.0",
				description = """
						REST API for the Service Uptime Monitor's dashboard backend (BFF layer).

						Responsibilities:
						- Aggregating current status data from the Monitoring Service
						- Providing a single overview endpoint with UP/DOWN counts
						- Proxying service history and registered-service metadata
						- Streaming real-time status updates via Server-Sent Events (SSE)

						The `/api/dashboard/stream` endpoint pushes the current status of all
						monitored services every 30 seconds. Clients must keep the connection open.
						""",
				contact = @Contact(
						name = "Stefan956",
						url = "https://github.com/Stefan956/serviceUptimeMonitor"
				)
		),
		servers = {
				@Server(url = "http://localhost:8083", description = "Local development"),
				@Server(url = "http://dashboard-service:8080", description = "Docker Compose")
		}
)
public class DashboardServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DashboardServiceApplication.class, args);
	}

}
