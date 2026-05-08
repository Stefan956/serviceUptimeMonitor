package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.scheduler;

import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.service.MonitoringService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("MonitoringScheduler Unit Tests")
class MonitoringSchedulerTest {

    @Mock
    private MonitoringService monitoringService;

    @InjectMocks
    private MonitoringScheduler monitoringScheduler;

    @Test
    @DisplayName("Should trigger monitoring cycle when scheduled method is called")
    void runMonitoringCycle_shouldCallMonitoringService() {
        // When
        monitoringScheduler.runMonitoringCycle();

        // Then
        verify(monitoringService).checkAllServices();
    }

}
