package com.github.Stefan956.serviceUptimeMonitor.alert_service;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
		info = @Info(
				title = "Alert Service API",
				version = "1.0.0",
				description = """
						REST API for the Service Uptime Monitor's alerting engine.

						Responsibilities:
						- Receiving status-change events from the Monitoring Service
						- Sending email notifications via JavaMailSender
						- Persisting alert history for audit and reporting
						- Providing alert query endpoints for the Dashboard Service

						The primary inbound endpoint (`POST /api/alerts/status-change`) is called
						internally by the Monitoring Service whenever a service transitions between
						UP and DOWN states.
						""",
				contact = @Contact(
						name = "Stefan956",
						url = "https://github.com/Stefan956/serviceUptimeMonitor"
				)
		),
		servers = {
				@Server(url = "http://localhost:8082", description = "Local development"),
				@Server(url = "http://alert-service:8080", description = "Docker Compose")
		}
)
public class AlertServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AlertServiceApplication.class, args);
	}

}
