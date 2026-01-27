// The app configuration is to be used later if needed
//package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.configuration;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
//import tools.jackson.databind.ObjectMapper;
//
//import java.time.Clock;
//
//@Configuration
//public class AppConfig {
//
//    /**
//     * Centralized clock – makes time testable
//     */
//    @Bean
//    public Clock clock() {
//        return Clock.systemUTC();
//    }
//
//    /**
//     * Scheduler thread pool for monitoring jobs
//     */
//    @Bean
//    public ThreadPoolTaskScheduler taskScheduler() {
//        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
//        scheduler.setPoolSize(5);
//        scheduler.setThreadNamePrefix("monitoring-scheduler-");
//        scheduler.initialize();
//        return scheduler;
//    }
//
//    /**
//     * JSON mapper (useful for alert payloads later)
//     */
//    @Bean
//    public ObjectMapper objectMapper() {
//        return new ObjectMapper();
//    }
//}
