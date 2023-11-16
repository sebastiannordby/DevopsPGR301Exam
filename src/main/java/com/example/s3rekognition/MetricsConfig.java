package com.example.s3rekognition;

import io.micrometer.cloudwatch2.CloudWatchConfig;
import io.micrometer.cloudwatch2.CloudWatchMeterRegistry;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;

@Configuration
public class MetricsConfig {

    @Value("${management.metrics.export.cloudwatch.namespace}")
    private String cloudwatchNamespace;

    @Value("${management.metrics.export.cloudwatch.step}")
    private String cloudwatchStep;

    @Bean
    public CloudWatchAsyncClient cloudWatchAsyncClient() {
        return CloudWatchAsyncClient
            .builder()
            .region(Region.EU_WEST_1)
            .build();
    }

    @Bean
    public MeterRegistry getMeterRegistry() {
        CloudWatchConfig cloudWatchConfig = new CloudWatchConfig() {
            @Override
            public String get(String key) {
                if ("cloudwatch.namespace".equals(key)) {
                    return cloudwatchNamespace;
                } else if ("cloudwatch.step".equals(key)) {
                    return cloudwatchStep;
                }
                return null;
            }
        };

        return new CloudWatchMeterRegistry(cloudWatchConfig, Clock.SYSTEM, cloudWatchAsyncClient());
    }
}
