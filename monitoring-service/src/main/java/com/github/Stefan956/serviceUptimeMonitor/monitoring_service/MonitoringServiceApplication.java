package com.github.Stefan956.serviceUptimeMonitor.monitoring_service;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@OpenAPIDefinition(
		info = @Info(
				title = "Monitoring Service API",
				version = "1.0.0",
				description = """
						REST API for the Service Uptime Monitor's core monitoring engine.

						Responsibilities:
						- Registering and managing monitored services (CRUD)
						- Scheduling and executing periodic HTTP health checks
						- Persisting check results and status history
						- Publishing status-change events to the Alert Service
						- Exposing read-only data for the Dashboard Service

						Prometheus metrics are available at `/actuator/prometheus`.
						""",
				contact = @Contact(
						name = "Stefan956",
						url = "https://github.com/Stefan956/serviceUptimeMonitor"
				)
		),
		servers = {
				@Server(url = "http://localhost:8080", description = "Local development"),
				@Server(url = "http://monitoring-service:8080", description = "Docker Compose")
		}
)
public class MonitoringServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MonitoringServiceApplication.class, args);
	}

}
