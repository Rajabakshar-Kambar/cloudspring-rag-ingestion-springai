package com.cloudspring.cloudspring_rag_ingestion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import software.amazon.awssdk.core.exception.SdkException;

import java.io.IOException;
import java.time.Duration;

@Configuration
public class RetryConfig {

    /**
     * Retries transient infrastructure failures (Bedrock/Qdrant network
     * blips, throttling, timeouts) with exponential backoff. Anything that
     * isn't an SdkException or IOException is treated as a programming
     * error and is not retried.
     */
    @Bean
    public RetryTemplate vectorStoreRetryTemplate() {

        RetryPolicy retryPolicy =
                RetryPolicy.builder()
                        .includes(SdkException.class)
                        .includes(IOException.class)
                        .maxRetries(4)
                        .delay(Duration.ofMillis(500))
                        .multiplier(2.0)
                        .maxDelay(Duration.ofSeconds(10))
                        .build();

        return new RetryTemplate(retryPolicy);
    }
}
