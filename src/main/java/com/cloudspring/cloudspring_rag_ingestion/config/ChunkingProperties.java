package com.cloudspring.cloudspring_rag_ingestion.config;

import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "app.chunking")
public class ChunkingProperties {

    @Min(1)
    private int chunkSize;

    @Min(1)
    private int minChunkSizeChars;

    @Min(1)
    private int minChunkLengthToEmbed;

    @Min(1)
    private int maxNumChunks;

    private boolean keepSeparator;

    /**
     * Kept for backwards compatibility with the pre-Spring-AI config.
     * TokenTextSplitter has no notion of overlap between chunks, so this
     * value is currently unused.
     */
    private int overlapSize;
}
