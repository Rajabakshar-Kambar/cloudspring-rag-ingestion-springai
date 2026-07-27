package com.cloudspring.cloudspring_rag_ingestion.exception;

public class DocumentTooLargeException extends NonRetryableIngestionException {

    public DocumentTooLargeException(String message) {
        super(message);
    }
}
