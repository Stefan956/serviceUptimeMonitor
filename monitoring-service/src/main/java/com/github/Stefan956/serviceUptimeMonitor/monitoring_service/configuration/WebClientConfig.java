package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }

    //for future configurations:
    //@Bean
    //public WebClient webClient() {
    //    HttpClient httpClient = HttpClient.create()
    //        .responseTimeout(Duration.ofSeconds(5));
    //
    //    return WebClient.builder()
    //        .clientConnector(new ReactorClientHttpConnector(httpClient))
    //        .build();
    //}
}


