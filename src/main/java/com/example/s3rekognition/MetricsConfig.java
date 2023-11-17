package com.example.s3rekognition;

import io.micrometer.cloudwatch2.CloudWatchConfig;
import io.micrometer.cloudwatch2.CloudWatchMeterRegistry;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;

import java.time.Duration;
import java.util.Map;

@Configuration
public class MetricsConfig {

    @Value("${management.metrics.export.cloudwatch.namespace}")
    private String cloudwatchNamespace;

    @Bean
    public CloudWatchAsyncClient cloudWatchAsyncClient() {
        return CloudWatchAsyncClient.builder()
            .region(Region.EU_WEST_1)
            .httpClientBuilder(NettyNioAsyncHttpClient.builder()
                .maxConcurrency(200) 
                .connectionMaxIdleTime(Duration.ofMinutes(1))
                .connectionAcquisitionTimeout(Duration.ofSeconds(60))
                .connectionTimeout(Duration.ofSeconds(60))
                .writeTimeout(Duration.ofSeconds(60)) 
                .readTimeout(Duration.ofSeconds(60))) 
            .build();
    }

    @Bean
    public MeterRegistry getMeterRegistry() {
        return new CloudWatchMeterRegistry(
            setupCloudWatchConfig(), 
            Clock.SYSTEM, 
            cloudWatchAsyncClient());
    }

    private CloudWatchConfig setupCloudWatchConfig() {
        CloudWatchConfig cloudWatchConfig = new CloudWatchConfig() {
            private Map<String, String> configuration = Map.of(
                "cloudwatch.namespace", 
                cloudwatchNamespace
                "cloudwatch.step", 
                Duration.ofSeconds(5).toString()
            );

            @Override
            public String get(String key) {
                return configuration.get(key);
            }
        };
        return cloudWatchConfig;
    }
}
