package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.scheduler;

import com.github.Stefan956.serviceUptimeMonitor.monitoring_service.service.MonitoringService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("MonitoringScheduler Unit Tests")
class MonitoringSchedulerTest {

    @Mock
    private MonitoringService monitoringService;

    @InjectMocks
    private MonitoringScheduler monitoringScheduler;

    @Test
    @DisplayName("Should delegate to MonitoringService on each tick")
    void runMonitoringCycle_shouldCallMonitoringService() {
        monitoringScheduler.runMonitoringCycle();

        verify(monitoringService).checkAllServices();
    }

    @Test
    @DisplayName("Should propagate exceptions from MonitoringService so Spring's scheduler can log and retry")
    void runMonitoringCycle_shouldPropagateExceptions() {
        doThrow(new RuntimeException("DB unavailable")).when(monitoringService).checkAllServices();

        assertThatThrownBy(() -> monitoringScheduler.runMonitoringCycle())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB unavailable");
    }
}
