package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.mapper;

import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto.MonitoredServiceRequestDto;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto.MonitoredServiceResponseDto;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.model.MonitoredService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MonitoredServiceMapper Unit Tests")
class MonitoredServiceMapperTest {

    private MonitoredServiceMapper mapper;
    private MonitoredService testService;
    private MonitoredServiceRequestDto testRequestDto;
    private MonitoredServiceResponseDto testResponseDto;

    @BeforeEach
    void setUp() {
        mapper = new MonitoredServiceMapper();

        UUID serviceId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        testService = new MonitoredService();
        testService.setId(serviceId);
        testService.setName("Test Service");
        testService.setUrl("http://test.com/health");
        testService.setCheckIntervalSeconds(60);
        testService.setEnabled(true);
        testService.setCreatedAt(now);
        testService.setUpdatedAt(null);

        testRequestDto = new MonitoredServiceRequestDto(
                "Test Service",
                "http://test.com/health",
                60
        );

        testResponseDto = new MonitoredServiceResponseDto(
                serviceId,
                "Test Service",
                "http://test.com/health",
                60,
                true,
                now,
                null
        );
    }

    @Test
    @DisplayName("Should map MonitoredService to MonitoredServiceResponseDto")
    void mapToMonitoredServiceResponseDto_shouldMapAllFields() {
        // When
        MonitoredServiceResponseDto result = mapper.toResponseDto(testService);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(testService.getId());
        assertThat(result.name()).isEqualTo(testService.getName());
        assertThat(result.url()).isEqualTo(testService.getUrl());
        assertThat(result.checkIntervalSeconds()).isEqualTo(testService.getCheckIntervalSeconds());
        assertThat(result.enabled()).isEqualTo(testService.isEnabled());
        assertThat(result.createdAt()).isEqualTo(testService.getCreatedAt());
        assertThat(result.updatedAt()).isEqualTo(testService.getUpdatedAt());
    }

    @Test
    @DisplayName("Should map MonitoredService with updatedAt to MonitoredServiceResponseDto")
    void mapToMonitoredServiceResponseDto_shouldMapWithUpdatedAt() {
        // Given
        LocalDateTime updatedAt = LocalDateTime.now();
        testService.setUpdatedAt(updatedAt);

        // When
        MonitoredServiceResponseDto result = mapper.toResponseDto(testService);

        // Then
        assertThat(result.updatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("Should map disabled service to MonitoredServiceResponseDto")
    void mapToMonitoredServiceResponseDto_shouldMapDisabledService() {
        // Given
        testService.setEnabled(false);

        // When
        MonitoredServiceResponseDto result = mapper.toResponseDto(testService);

        // Then
        assertThat(result.enabled()).isFalse();
    }

    @Test
    @DisplayName("Should map entity with null updatedAt to response dto")
    void toResponseDto_shouldHandleNullUpdatedAt() {
        // Given
        testService.setUpdatedAt(null);

        // When
        MonitoredServiceResponseDto result = mapper.toResponseDto(testService);

        // Then
        assertThat(result.updatedAt()).isNull();
    }

    @Test
    @DisplayName("Should create new entity from MonitoredServiceRequestDto")
    void fromCreateRequest_shouldCreateNewEntity() {
        // When
        MonitoredService result = mapper.fromCreateRequest(testRequestDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(testRequestDto.name());
        assertThat(result.getUrl()).isEqualTo(testRequestDto.url());
        assertThat(result.getCheckIntervalSeconds()).isEqualTo(testRequestDto.checkIntervalSeconds());
        assertThat(result.isEnabled()).isTrue();
        assertThat(result.getId()).isNull(); // ID should not be set in create
    }

    @Test
    @DisplayName("Should update existing entity from MonitoredServiceRequestDto")
    void updateEntity_shouldUpdateAllFields() {
        // Given
        MonitoredServiceRequestDto updateDto = new MonitoredServiceRequestDto(
                "Updated Service",
                "http://updated.com/health",
                120
        );

        // When
        mapper.updateEntity(testService, updateDto);

        // Then
        assertThat(testService.getName()).isEqualTo("Updated Service");
        assertThat(testService.getUrl()).isEqualTo("http://updated.com/health");
        assertThat(testService.getCheckIntervalSeconds()).isEqualTo(120);
    }

    @Test
    @DisplayName("Should preserve id and enabled when updating entity")
    void updateEntity_shouldPreserveIdAndEnabled() {
        // Given
        UUID originalId = testService.getId();
        boolean originalEnabled = testService.isEnabled();

        MonitoredServiceRequestDto updateDto = new MonitoredServiceRequestDto(
                "Updated Service",
                "http://updated.com/health",
                120
        );

        // When
        mapper.updateEntity(testService, updateDto);

        // Then
        assertThat(testService.getId()).isEqualTo(originalId);
        assertThat(testService.isEnabled()).isEqualTo(originalEnabled);
    }

    @Test
    @DisplayName("Should handle mapping with special characters in name and URL")
    void fromCreateRequest_shouldHandleSpecialCharacters() {
        // Given
        MonitoredServiceRequestDto dto = new MonitoredServiceRequestDto(
                "Service & Test (v2)",
                "http://test.com/health?param=value&other=123",
                60
        );

        // When
        MonitoredService result = mapper.fromCreateRequest(dto);

        // Then
        assertThat(result.getName()).isEqualTo("Service & Test (v2)");
        assertThat(result.getUrl()).isEqualTo("http://test.com/health?param=value&other=123");
    }
}
