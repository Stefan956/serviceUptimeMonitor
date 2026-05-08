package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.repository;

import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dao.MonitoredServiceRepository;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.model.MonitoredService;
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
class MonitoredServiceRepositoryIntegrationTest {

    @Autowired
    private MonitoredServiceRepository repository;

    private MonitoredService enabledService;
    private MonitoredService disabledService;

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        enabledService = new MonitoredService();
        enabledService.setName("Enabled Service");
        enabledService.setUrl("http://enabled.com/health");
        enabledService.setCheckIntervalSeconds(60);
        enabledService.setEnabled(true);
        enabledService.setCreatedAt(LocalDateTime.now());
        enabledService = repository.save(enabledService);

        disabledService = new MonitoredService();
        disabledService.setName("Disabled Service");
        disabledService.setUrl("http://disabled.com/health");
        disabledService.setCheckIntervalSeconds(30);
        disabledService.setEnabled(false);
        disabledService.setCreatedAt(LocalDateTime.now());
        disabledService = repository.save(disabledService);
    }

    @Test
    @DisplayName("findByEnabledTrue returns only enabled services")
    void findByEnabledTrue_returnsOnlyEnabledServices() {
        List<MonitoredService> result = repository.findByEnabledTrue();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Enabled Service");
        assertThat(result.get(0).isEnabled()).isTrue();
    }

    @Test
    @DisplayName("findByEnabledTrue returns empty list when no services are enabled")
    void findByEnabledTrue_returnsEmptyWhenNoneEnabled() {
        enabledService.setEnabled(false);
        repository.save(enabledService);

        List<MonitoredService> result = repository.findByEnabledTrue();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByEnabledTrue returns all when all services are enabled")
    void findByEnabledTrue_returnsAllWhenAllEnabled() {
        disabledService.setEnabled(true);
        repository.save(disabledService);

        List<MonitoredService> result = repository.findByEnabledTrue();

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("save persists a MonitoredService with generated ID")
    void save_persistsServiceWithGeneratedId() {
        MonitoredService newService = new MonitoredService();
        newService.setName("New Service");
        newService.setUrl("http://new.com/health");
        newService.setCheckIntervalSeconds(120);
        newService.setEnabled(true);
        newService.setCreatedAt(LocalDateTime.now());

        MonitoredService saved = repository.save(newService);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("New Service");
    }

    @Test
    @DisplayName("findById returns the correct service")
    void findById_returnsCorrectService() {
        Optional<MonitoredService> found = repository.findById(enabledService.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Enabled Service");
        assertThat(found.get().getUrl()).isEqualTo("http://enabled.com/health");
    }

    @Test
    @DisplayName("findById returns empty for non-existent ID")
    void findById_returnsEmptyForNonExistentId() {
        Optional<MonitoredService> found = repository.findById(UUID.randomUUID());

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("findAll returns all services regardless of enabled status")
    void findAll_returnsAllServices() {
        List<MonitoredService> result = repository.findAll();

        assertThat(result).hasSize(2);
    }
}
