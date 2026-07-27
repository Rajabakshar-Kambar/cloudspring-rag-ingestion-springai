package com.cloudspring.cloudspring_rag_ingestion.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.Set;

@Data
@Validated
@ConfigurationProperties(prefix = "app.ingestion")
public class IngestionProperties {

    /**
     * File extensions (without the dot, case-insensitive) this service will
     * process. Anything else is rejected without retrying.
     */
    @NotEmpty
    private Set<String> supportedExtensions;

    /**
     * Upper bound on the S3 object size this service will download, checked
     * via a HeadObject call before the file is pulled into memory.
     */
    @Min(1)
    private long maxFileSizeMb;

    /**
     * Maximum number of SQS delivery attempts before a message is treated
     * as permanently failed and acknowledged instead of retried again.
     * Keep this at or below the queue's redrive policy maxReceiveCount.
     */
    @Min(1)
    private int maxRetries;

    public boolean isSupportedExtension(String extension) {

        return extension != null
                && supportedExtensions.contains(extension.toLowerCase());
    }

    public long maxFileSizeBytes() {

        return maxFileSizeMb * 1024 * 1024;
    }
}
