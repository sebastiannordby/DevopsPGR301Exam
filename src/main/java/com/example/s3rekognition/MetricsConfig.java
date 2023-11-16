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
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;

import java.time.Duration;
import java.util.Map;

/*
* Formidabel kudos til Glenn Bech:
* https://github.com/glennbechdevops/terraform-cloudwatch-dashboard/blob/main/src/main/java/com/example/demo/MetricsConfig.java
* */

@Configuration
public class MetricsConfig {
    @Value("${management.metrics.export.cloudwatch.namespace}")
    private String cloudWatchNamespace;

    @Value("${management.metrics.export.cloudwatch.step}")
    private String cloudWatchStep;

    @Bean
    public CloudWatchAsyncClient cloudWatchAsyncClient() {
        return CloudWatchAsyncClient
            .builder()
            .region(Region.EU_WEST_1)
            .httpClientBuilder(NettyNioAsyncHttpClient.builder()
                .maxConcurrency(100) 
                .connectionTimeout(Duration.ofSeconds(30))
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
        return new CloudWatchConfig() {
            private Map<String, String> configuration = Map.of(
                "cloudwatch.namespace",
                cloudWatchNamespace,
                "cloudwatch.step",
                cloudWatchStep);

                @Override
                public String get(String key) {
                    return configuration.get(key);
                }
        };
    }
}