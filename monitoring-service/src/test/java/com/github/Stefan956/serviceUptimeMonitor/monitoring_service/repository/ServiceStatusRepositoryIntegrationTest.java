package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.repository;

import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dao.MonitoredServiceRepository;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dao.ServiceStatusRepository;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ServiceStatusRepositoryIntegrationTest {

    @Autowired
    private ServiceStatusRepository statusRepository;

    @Autowired
    private MonitoredServiceRepository serviceRepository;

    private MonitoredService testService;

    @BeforeEach
    void setUp() {
        statusRepository.deleteAll();
        serviceRepository.deleteAll();

        testService = new MonitoredService();
        testService.setName("Test Service");
        testService.setUrl("http://test.com/health");
        testService.setCheckIntervalSeconds(60);
        testService.setEnabled(true);
        testService.setCreatedAt(LocalDateTime.now());
        testService = serviceRepository.save(testService);
    }

    @Test
    @DisplayName("findTopByMonitoredServiceOrderByCheckedAtDesc returns the latest status")
    void findLatestStatus_returnsNewest() {
        createStatus(testService, ServiceHealthStatus.UP, 200, LocalDateTime.now().minusMinutes(10));
        ServiceStatus newer = createStatus(testService, ServiceHealthStatus.DOWN, 0, LocalDateTime.now());

        Optional<ServiceStatus> result = statusRepository.findTopByMonitoredServiceOrderByCheckedAtDesc(testService);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(newer.getId());
        assertThat(result.get().getStatus()).isEqualTo(ServiceHealthStatus.DOWN);
    }

    @Test
    @DisplayName("findTopByMonitoredServiceOrderByCheckedAtDesc returns empty when no statuses exist")
    void findLatestStatus_returnsEmptyWhenNoStatuses() {
        Optional<ServiceStatus> result = statusRepository.findTopByMonitoredServiceOrderByCheckedAtDesc(testService);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByMonitoredServiceIdOrderByCheckedAtDesc returns statuses ordered by checkedAt desc")
    void findHistory_returnsOrderedByCheckedAtDesc() {
        LocalDateTime now = LocalDateTime.now();
        createStatus(testService, ServiceHealthStatus.UP, 200, now.minusMinutes(15));
        createStatus(testService, ServiceHealthStatus.DOWN, 0, now.minusMinutes(10));
        createStatus(testService, ServiceHealthStatus.UP, 200, now.minusMinutes(5));

        List<ServiceStatus> result = statusRepository.findByMonitoredServiceIdOrderByCheckedAtDesc(testService.getId());

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getCheckedAt()).isAfter(result.get(1).getCheckedAt());
        assertThat(result.get(1).getCheckedAt()).isAfter(result.get(2).getCheckedAt());
    }

    @Test
    @DisplayName("findByMonitoredServiceIdOrderByCheckedAtDesc returns empty for non-existent service")
    void findHistory_returnsEmptyForNonExistentService() {
        List<ServiceStatus> result = statusRepository.findByMonitoredServiceIdOrderByCheckedAtDesc(UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByMonitoredServiceIdOrderByCheckedAtDesc does not return statuses of other services")
    void findHistory_doesNotReturnOtherServiceStatuses() {
        MonitoredService otherService = new MonitoredService();
        otherService.setName("Other Service");
        otherService.setUrl("http://other.com/health");
        otherService.setCheckIntervalSeconds(30);
        otherService.setEnabled(true);
        otherService.setCreatedAt(LocalDateTime.now());
        otherService = serviceRepository.save(otherService);

        createStatus(testService, ServiceHealthStatus.UP, 200, LocalDateTime.now());
        createStatus(otherService, ServiceHealthStatus.DOWN, 0, LocalDateTime.now());

        List<ServiceStatus> result = statusRepository.findByMonitoredServiceIdOrderByCheckedAtDesc(testService.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMonitoredService().getId()).isEqualTo(testService.getId());
    }

    private ServiceStatus createStatus(MonitoredService service, ServiceHealthStatus healthStatus,
                                        int httpStatusCode, LocalDateTime checkedAt) {
        ServiceStatus status = new ServiceStatus();
        status.setMonitoredService(service);
        status.setStatus(healthStatus);
        status.setHttpStatusCode(httpStatusCode);
        status.setResponseTimeMs(healthStatus == ServiceHealthStatus.UP ? 100 : 0);
        status.setCheckedAt(checkedAt);
        return statusRepository.save(status);
    }
}
