package com.cloudspring.cloudspring_rag_ingestion.exception;

/**
 * Base type for every failure that can occur while ingesting a document.
 * <p>
 * Subclasses fall into two families:
 * <ul>
 *     <li>{@link NonRetryableIngestionException} - the input itself is the
 *     problem (wrong file type, too large, empty, missing). Retrying without
 *     a human fixing something will never succeed, so the listener logs and
 *     acknowledges the SQS message instead of retrying it forever.</li>
 *     <li>{@link TransientIngestionException} - an infrastructure hiccup
 *     (S3, Bedrock, Qdrant, network). Retrying may well succeed, so the
 *     listener lets SQS redeliver the message (up to the queue's redrive
 *     policy / {@code app.ingestion.max-retries}).</li>
 * </ul>
 */
public abstract class DocumentIngestionException extends RuntimeException {

    protected DocumentIngestionException(String message) {
        super(message);
    }

    protected DocumentIngestionException(String message, Throwable cause) {
        super(message, cause);
    }
}
