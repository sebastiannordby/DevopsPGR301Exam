package com.example.s3rekognition;

import io.micrometer.cloudwatch2.CloudWatchConfig;
import io.micrometer.cloudwatch2.CloudWatchMeterRegistry;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;

import java.time.Duration;
import java.util.Map;

/*
* Formidabel kudos til Glenn Bech:
* https://github.com/glennbechdevops/terraform-cloudwatch-dashboard/blob/main/src/main/java/com/example/demo/MetricsConfig.java
* */

@Configuration
public class MetricsConfig {
    @Bean
    public CloudWatchAsyncClient cloudWatchAsyncClient() {
        return CloudWatchAsyncClient
            .builder()
            .region(Region.EU_WEST_1)
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
                "",
                "cloudwatch.step",
                Duration.ofSeconds(5).toString());

                @Override
                public String get(String key) {
                    return configuration.get(key);
                }
        };
    }
}