package com.cloudspring.cloudspring_rag_ingestion.exception;

/**
 * A failure caused by the input itself (bad file type, oversized file, empty
 * PDF, missing S3 object). Retrying without changing the input will always
 * fail the same way, so the listener should log this and acknowledge the
 * SQS message rather than let it be redelivered.
 */
public class NonRetryableIngestionException extends DocumentIngestionException {

    public NonRetryableIngestionException(String message) {
        super(message);
    }

    public NonRetryableIngestionException(String message, Throwable cause) {
        super(message, cause);
    }
}
