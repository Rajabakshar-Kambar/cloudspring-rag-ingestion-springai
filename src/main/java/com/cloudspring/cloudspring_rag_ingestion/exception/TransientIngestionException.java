package com.cloudspring.cloudspring_rag_ingestion.exception;

/**
 * A failure caused by infrastructure (S3, Bedrock, Qdrant, network timeouts,
 * throttling). The same input would likely succeed on a later attempt, so
 * the listener lets this propagate and SQS redelivers the message.
 */
public class TransientIngestionException extends DocumentIngestionException {

    public TransientIngestionException(String message, Throwable cause) {
        super(message, cause);
    }
}
