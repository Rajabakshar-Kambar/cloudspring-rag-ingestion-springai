package com.cloudspring.cloudspring_rag_ingestion.listener;

import com.cloudspring.cloudspring_rag_ingestion.config.IngestionProperties;
import com.cloudspring.cloudspring_rag_ingestion.exception.NonRetryableIngestionException;
import com.cloudspring.cloudspring_rag_ingestion.model.DocumentMessage;
import com.cloudspring.cloudspring_rag_ingestion.service.ChunkingService;
import com.cloudspring.cloudspring_rag_ingestion.service.DocumentValidationService;
import com.cloudspring.cloudspring_rag_ingestion.service.PdfExtractorService;
import com.cloudspring.cloudspring_rag_ingestion.service.S3DocumentService;
import com.cloudspring.cloudspring_rag_ingestion.service.VectorStoreIngestionService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.listener.SqsHeaders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.ai.document.Document;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentIngestionListener {

    private static final String MDC_DOCUMENT_KEY = "documentKey";
    private static final String MDC_VERSION_ID = "versionId";

    private final IngestionProperties ingestionProperties;
    private final DocumentValidationService validationService;
    private final S3DocumentService s3Service;
    private final PdfExtractorService pdfService;
    private final ChunkingService chunkingService;
    private final VectorStoreIngestionService vectorStoreIngestionService;

    @SqsListener("${app.sqs.queue-name}")
    public void receive(
            DocumentMessage message,
            @Header(SqsHeaders.MessageSystemAttributes.SQS_APPROXIMATE_RECEIVE_COUNT)
            int receiveCount) {

        MDC.put(MDC_DOCUMENT_KEY, message.objectKey());
        MDC.put(MDC_VERSION_ID, message.versionId());

        try {

            processWithRetryBudget(message, receiveCount);

        } finally {

            MDC.remove(MDC_DOCUMENT_KEY);
            MDC.remove(MDC_VERSION_ID);
        }
    }

    private void processWithRetryBudget(
            DocumentMessage message,
            int receiveCount) {

        try {

            process(message);

        } catch (NonRetryableIngestionException e) {

            // Bad input (wrong type, too large, empty, missing) - retrying
            // won't help. Log and let the message be acknowledged so it
            // doesn't loop forever.
            log.error(
                    "Giving up on {} (non-retryable): {}",
                    message.objectKey(),
                    e.getMessage());

        } catch (RuntimeException e) {

            // Infra hiccup. If we still have retry budget left, rethrow so
            // SQS redelivers the message; otherwise give up and log loudly
            // so it shows up in alerting rather than silently vanishing
            // into a DLQ.
            if (receiveCount < ingestionProperties.getMaxRetries()) {

                log.warn(
                        "Attempt {}/{} failed for {}, will retry: {}",
                        receiveCount,
                        ingestionProperties.getMaxRetries(),
                        message.objectKey(),
                        e.getMessage());

                throw e;
            }

            log.error(
                    "Giving up on {} after {} attempts",
                    message.objectKey(),
                    receiveCount,
                    e);
        }
    }

    private void process(DocumentMessage message) {

        long start = System.currentTimeMillis();

        log.info("Processing {}", message.objectKey());

        validationService.validate(
                message.bucketName(),
                message.objectKey(),
                message.versionId());

        byte[] pdfBytes =
                s3Service.download(
                        message.bucketName(),
                        message.objectKey(),
                        message.versionId());

        Document sourceDocument =
                pdfService.extract(
                        pdfBytes,
                        message.objectKey());

        List<Document> chunks =
                chunkingService.chunk(
                        sourceDocument,
                        message.objectKey(),
                        message.versionId());

        vectorStoreIngestionService.replaceChunks(
                message.objectKey(),
                chunks);

        log.info(
                "Completed {}: {} chunks in {} ms",
                message.objectKey(),
                chunks.size(),
                System.currentTimeMillis() - start);
    }
}
