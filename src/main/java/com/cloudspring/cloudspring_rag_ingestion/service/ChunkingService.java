package com.cloudspring.cloudspring_rag_ingestion.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChunkingService {

    private final TokenTextSplitter tokenTextSplitter;

    public List<Document> chunk(
            Document sourceDocument,
            String documentName,
            String versionId) {

        List<Document> rawChunks =
                tokenTextSplitter.apply(
                        List.of(sourceDocument));

        if (rawChunks.isEmpty()) {

            log.warn(
                    "Splitter produced 0 chunks for {} - nothing will be indexed",
                    documentName);

            return List.of();
        }

        List<Document> chunks = new ArrayList<>();
        int index = 0;

        for (Document rawChunk : rawChunks) {

            Map<String, Object> metadata =
                    new HashMap<>(rawChunk.getMetadata());

            metadata.put("documentName", documentName);
            metadata.put("versionId", versionId);
            metadata.put("chunkIndex", index++);

            chunks.add(
                    Document.builder()
                            .text(rawChunk.getText())
                            .metadata(metadata)
                            .build());
        }

        return chunks;
    }
}
