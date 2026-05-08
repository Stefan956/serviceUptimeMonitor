package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.service;

import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dao.MonitoredServiceRepository;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto.MonitoredServiceRequestDto;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto.MonitoredServiceResponseDto;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.mapper.MonitoredServiceMapper;
import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.model.MonitoredService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
@DisplayName("MonitoredServiceManagementService Unit Tests")
class MonitoredServiceManagementServiceTest {

    @Mock
    private MonitoredServiceRepository serviceRepository;

    @Mock
    private MonitoredServiceMapper mapper;

    @InjectMocks
    private MonitoredServiceManagementService managementService;

    private MonitoredService testService;
    private MonitoredServiceRequestDto requestDto;
    private UUID testServiceId;

    @BeforeEach
    void setUp() {
        testServiceId = UUID.randomUUID();

        testService = new MonitoredService();
        testService.setId(testServiceId);
        testService.setName("Test Service");
        testService.setUrl("http://example.com/health");
        testService.setCheckIntervalSeconds(60);
        testService.setEnabled(true);
        testService.setCreatedAt(LocalDateTime.now());

        requestDto = new MonitoredServiceRequestDto(
                "Test Service",
                "http://example.com/health",
                60
        );
    }

    @Test
    @DisplayName("Should create a new monitored service")
    void create_shouldCreateNewService() {
        // Given
        when(mapper.fromCreateRequest(requestDto)).thenReturn(testService);
        when(serviceRepository.save(any(MonitoredService.class))).thenReturn(testService);
        when(mapper.toResponseDto(testService)).thenReturn(new MonitoredServiceResponseDto(
                testService.getId(), testService.getName(), testService.getUrl(),
                testService.getCheckIntervalSeconds(), testService.isEnabled(),
                testService.getCreatedAt(), null));
        ArgumentCaptor<MonitoredService> serviceCaptor = ArgumentCaptor.forClass(MonitoredService.class);

        // When
        MonitoredServiceResponseDto result = managementService.create(requestDto);

        // Then
        verify(serviceRepository).save(serviceCaptor.capture());
        MonitoredService savedService = serviceCaptor.getValue();

        assertThat(savedService.getName()).isEqualTo(requestDto.name());
        assertThat(savedService.getUrl()).isEqualTo(requestDto.url());
        assertThat(savedService.getCheckIntervalSeconds()).isEqualTo(requestDto.checkIntervalSeconds());
        assertThat(savedService.isEnabled()).isTrue();
        assertThat(savedService.getCreatedAt()).isNotNull();

        assertThat(result.id()).isEqualTo(testService.getId());
        assertThat(result.name()).isEqualTo(testService.getName());
        assertThat(result.url()).isEqualTo(testService.getUrl());
    }

    @Test
    @DisplayName("Should update an existing monitored service")
    void update_shouldUpdateExistingService() {
        // Given
        MonitoredServiceRequestDto updateRequest = new MonitoredServiceRequestDto(
                "Updated Service",
                "http://updated.com/health",
                120
        );

        when(serviceRepository.findById(testServiceId)).thenReturn(Optional.of(testService));
        doAnswer(inv -> {
            MonitoredService s = inv.getArgument(0);
            MonitoredServiceRequestDto dto = inv.getArgument(1);
            s.setName(dto.name());
            s.setUrl(dto.url());
            s.setCheckIntervalSeconds(dto.checkIntervalSeconds());
            return null;
        }).when(mapper).updateEntity(eq(testService), eq(updateRequest));
        when(mapper.toResponseDto(testService)).thenReturn(new MonitoredServiceResponseDto(
                testService.getId(), "Updated Service", "http://updated.com/health",
                120, testService.isEnabled(), testService.getCreatedAt(), null));

        // When
        MonitoredServiceResponseDto result = managementService.update(testServiceId, updateRequest);

        // Then
        assertThat(testService.getName()).isEqualTo("Updated Service");
        assertThat(testService.getUrl()).isEqualTo("http://updated.com/health");
        assertThat(testService.getCheckIntervalSeconds()).isEqualTo(120);
        assertThat(testService.getUpdatedAt()).isNotNull();

        assertThat(result.name()).isEqualTo("Updated Service");
        assertThat(result.url()).isEqualTo("http://updated.com/health");
        assertThat(result.checkIntervalSeconds()).isEqualTo(120);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent service")
    void update_shouldThrowException_whenServiceNotFound() {
        // Given
        when(serviceRepository.findById(testServiceId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> managementService.update(testServiceId, requestDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Service not found");
    }

    @Test
    @DisplayName("Should enable a disabled service")
    void enable_shouldEnableService() {
        // Given
        testService.setEnabled(false);
        when(serviceRepository.findById(testServiceId)).thenReturn(Optional.of(testService));

        // When
        managementService.enable(testServiceId);

        // Then
        assertThat(testService.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("Should throw exception when enabling non-existent service")
    void enable_shouldThrowException_whenServiceNotFound() {
        // Given
        when(serviceRepository.findById(testServiceId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> managementService.enable(testServiceId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Service not found");
    }

    @Test
    @DisplayName("Should disable an enabled service")
    void disable_shouldDisableService() {
        // Given
        testService.setEnabled(true);
        when(serviceRepository.findById(testServiceId)).thenReturn(Optional.of(testService));

        // When
        managementService.disable(testServiceId);

        // Then
        assertThat(testService.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("Should throw exception when disabling non-existent service")
    void disable_shouldThrowException_whenServiceNotFound() {
        // Given
        when(serviceRepository.findById(testServiceId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> managementService.disable(testServiceId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Service not found");
    }

    @Test
    @DisplayName("Should get all monitored services")
    void getAll_shouldReturnAllServices() {
        // Given
        MonitoredService service1 = createService("Service 1", "http://service1.com");
        MonitoredService service2 = createService("Service 2", "http://service2.com");

        when(serviceRepository.findAll()).thenReturn(List.of(service1, service2));
        when(mapper.toResponseDto(service1)).thenReturn(new MonitoredServiceResponseDto(
                service1.getId(), "Service 1", "http://service1.com", 60, true, service1.getCreatedAt(), null));
        when(mapper.toResponseDto(service2)).thenReturn(new MonitoredServiceResponseDto(
                service2.getId(), "Service 2", "http://service2.com", 60, true, service2.getCreatedAt(), null));

        // When
        List<MonitoredServiceResponseDto> result = managementService.getAll();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("Service 1");
        assertThat(result.get(1).name()).isEqualTo("Service 2");
    }

    @Test
    @DisplayName("Should get service by id")
    void getById_shouldReturnService() {
        // Given
        when(serviceRepository.findById(testServiceId)).thenReturn(Optional.of(testService));
        when(mapper.toResponseDto(testService)).thenReturn(new MonitoredServiceResponseDto(
                testService.getId(), testService.getName(), testService.getUrl(),
                testService.getCheckIntervalSeconds(), testService.isEnabled(),
                testService.getCreatedAt(), null));

        // When
        MonitoredServiceResponseDto result = managementService.getById(testServiceId);

        // Then
        assertThat(result.id()).isEqualTo(testService.getId());
        assertThat(result.name()).isEqualTo(testService.getName());
        assertThat(result.url()).isEqualTo(testService.getUrl());
        assertThat(result.checkIntervalSeconds()).isEqualTo(testService.getCheckIntervalSeconds());
        assertThat(result.enabled()).isEqualTo(testService.isEnabled());
    }

    @Test
    @DisplayName("Should throw exception when getting non-existent service")
    void getById_shouldThrowException_whenServiceNotFound() {
        // Given
        when(serviceRepository.findById(testServiceId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> managementService.getById(testServiceId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Service not found");
    }

    @Test
    @DisplayName("Should return empty list when no services exist")
    void getAll_shouldReturnEmptyList_whenNoServices() {
        // Given
        when(serviceRepository.findAll()).thenReturn(List.of());
        // mapper not called when list is empty

        // When
        List<MonitoredServiceResponseDto> result = managementService.getAll();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should delete an existing service")
    void delete_shouldDeleteService() {
        // Given
        when(serviceRepository.findById(testServiceId)).thenReturn(Optional.of(testService));

        // When
        managementService.delete(testServiceId);

        // Then
        verify(serviceRepository).delete(testService);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent service")
    void delete_shouldThrowException_whenServiceNotFound() {
        // Given
        when(serviceRepository.findById(testServiceId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> managementService.delete(testServiceId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Service not found");

        verify(serviceRepository, never()).delete(any());
    }

    // Helper method
    private MonitoredService createService(String name, String url) {
        MonitoredService service = new MonitoredService();
        service.setId(UUID.randomUUID());
        service.setName(name);
        service.setUrl(url);
        service.setCheckIntervalSeconds(60);
        service.setEnabled(true);
        service.setCreatedAt(LocalDateTime.now());
        return service;
    }
}
