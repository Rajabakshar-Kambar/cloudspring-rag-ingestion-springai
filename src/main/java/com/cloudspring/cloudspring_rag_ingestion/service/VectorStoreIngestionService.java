package com.cloudspring.cloudspring_rag_ingestion.service;

import com.cloudspring.cloudspring_rag_ingestion.exception.TransientIngestionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorStoreIngestionService {

    private final VectorStore vectorStore;
    private final RetryTemplate vectorStoreRetryTemplate;

    private static final FilterExpressionBuilder FILTER =
            new FilterExpressionBuilder();

    /**
     * Replaces all chunks for a document in one call: deletes whatever is
     * already indexed under {@code documentName}, then embeds and upserts
     * the new chunks. This makes re-processing the same document (e.g. a
     * redelivered SQS message, or a new version of the same file) safe to
     * run repeatedly without leaving stale or duplicate chunks behind.
     * <p>
     * Both steps run through a retry template - the VectorStore call is
     * what actually invokes the Bedrock Titan EmbeddingModel under the
     * hood, so transient Bedrock/Qdrant failures are retried here rather
     * than bubbling straight into an SQS redelivery.
     */
    public void replaceChunks(
            String documentName,
            List<Document> chunks) {

        try {

            vectorStoreRetryTemplate.invoke(() -> {

                vectorStore.delete(
                        FILTER.eq("documentName", documentName).build());

                if (!chunks.isEmpty()) {
                    vectorStore.add(chunks);
                }
            });

        } catch (RuntimeException e) {

            throw new TransientIngestionException(
                    "Failed to write " + chunks.size() + " chunks for "
                            + documentName + " after retries",
                    e);
        }

        log.info(
                "Replaced chunks for {}: {} inserted",
                documentName,
                chunks.size());
    }
}