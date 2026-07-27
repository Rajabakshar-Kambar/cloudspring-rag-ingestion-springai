package com.cloudspring.cloudspring_rag_ingestion.config;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class TextSplitterConfig {

    private final ChunkingProperties chunkingProperties;

    @Bean
    public TokenTextSplitter tokenTextSplitter() {

        // TokenTextSplitter's builder happens to take exactly the fields
        // ChunkingProperties already exposes (chunkSize, minChunkSizeChars,
        // minChunkLengthToEmbed, maxNumChunks, keepSeparator).
        //
        // NOTE: TokenTextSplitter splits on *token* count, not raw characters
        // like the old ChunkingService did, and it has no notion of
        // "overlapSize" between chunks. app.chunking.overlap-size is kept in
        // ChunkingProperties for backwards compatibility but is not used here.
        return TokenTextSplitter.builder()
                .withChunkSize(chunkingProperties.getChunkSize())
                .withMinChunkSizeChars(chunkingProperties.getMinChunkSizeChars())
                .withMinChunkLengthToEmbed(chunkingProperties.getMinChunkLengthToEmbed())
                .withMaxNumChunks(chunkingProperties.getMaxNumChunks())
                .withKeepSeparator(chunkingProperties.isKeepSeparator())
                .build();
    }
}