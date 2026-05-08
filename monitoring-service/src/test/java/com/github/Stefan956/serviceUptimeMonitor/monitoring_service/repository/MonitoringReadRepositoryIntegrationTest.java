package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.repository;

import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dao.MonitoredServiceRepository;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dao.MonitoringReadRepository;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dao.ServiceStatusRepository;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto.ServiceStatusSummaryDto;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.model.MonitoredService;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.model.ServiceHealthStatus;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.model.ServiceStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MonitoringReadRepositoryIntegrationTest {

    @Autowired
    private MonitoringReadRepository readRepository;

    @Autowired
    private MonitoredServiceRepository serviceRepository;

    @Autowired
    private ServiceStatusRepository statusRepository;

    @BeforeEach
    void setUp() {
        statusRepository.deleteAll();
        serviceRepository.deleteAll();
    }

    @Test
    @DisplayName("findCurrentStatusPerService returns latest status for each service")
    void findCurrentStatusPerService_returnsLatestStatusPerService() {
        MonitoredService service1 = createService("Service A", "http://a.com/health");
        MonitoredService service2 = createService("Service B", "http://b.com/health");

        LocalDateTime now = LocalDateTime.now();

        // Service A: older UP, newer DOWN → should return DOWN
        createStatus(service1, ServiceHealthStatus.UP, 200, 50, now.minusMinutes(10));
        createStatus(service1, ServiceHealthStatus.DOWN, 0, 0, now.minusMinutes(1));

        // Service B: older DOWN, newer UP → should return UP
        createStatus(service2, ServiceHealthStatus.DOWN, 0, 0, now.minusMinutes(10));
        createStatus(service2, ServiceHealthStatus.UP, 200, 80, now.minusMinutes(1));

        List<ServiceStatusSummaryDto> result = readRepository.findCurrentStatusPerService();

        assertThat(result).hasSize(2);

        ServiceStatusSummaryDto serviceA = result.stream()
                .filter(s -> s.serviceName().equals("Service A"))
                .findFirst()
                .orElseThrow();
        assertThat(serviceA.status()).isEqualTo(ServiceHealthStatus.DOWN);

        ServiceStatusSummaryDto serviceB = result.stream()
                .filter(s -> s.serviceName().equals("Service B"))
                .findFirst()
                .orElseThrow();
        assertThat(serviceB.status()).isEqualTo(ServiceHealthStatus.UP);
        assertThat(serviceB.httpStatusCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("findCurrentStatusPerService returns empty when no statuses exist")
    void findCurrentStatusPerService_returnsEmptyWhenNoStatuses() {
        createService("Lonely Service", "http://lonely.com/health");

        List<ServiceStatusSummaryDto> result = readRepository.findCurrentStatusPerService();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findCurrentStatusPerService returns single entry for service with one status")
    void findCurrentStatusPerService_returnsSingleEntry() {
        MonitoredService service = createService("Single Service", "http://single.com/health");
        createStatus(service, ServiceHealthStatus.UP, 200, 42, LocalDateTime.now());

        List<ServiceStatusSummaryDto> result = readRepository.findCurrentStatusPerService();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).serviceName()).isEqualTo("Single Service");
        assertThat(result.get(0).status()).isEqualTo(ServiceHealthStatus.UP);
        assertThat(result.get(0).responseTimeMs()).isEqualTo(42);
    }

    private MonitoredService createService(String name, String url) {
        MonitoredService service = new MonitoredService();
        service.setName(name);
        service.setUrl(url);
        service.setCheckIntervalSeconds(60);
        service.setEnabled(true);
        service.setCreatedAt(LocalDateTime.now());
        return serviceRepository.save(service);
    }

    private ServiceStatus createStatus(MonitoredService service, ServiceHealthStatus healthStatus,
                                        int httpStatusCode, long responseTimeMs, LocalDateTime checkedAt) {
        ServiceStatus status = new ServiceStatus();
        status.setMonitoredService(service);
        status.setStatus(healthStatus);
        status.setHttpStatusCode(httpStatusCode);
        status.setResponseTimeMs(responseTimeMs);
        status.setCheckedAt(checkedAt);
        return statusRepository.save(status);
    }
}
