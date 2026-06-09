package com.github.Stefan956.serviceUptimeMonitor.dashboard_service.configuration;

import com.github.Stefan956.serviceUptimeMonitor.dashboard_service.util.Constants;
import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Value("${monitoring.service.url}")
    private String monitoringServiceUrl;

    @Bean
    public WebClient monitoringWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Constants.DEFAULT_TIMEOUT);
        return WebClient.builder()
                .baseUrl(monitoringServiceUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
