package com.cloudspring.cloudspring_rag_ingestion.exception;

public class DocumentNotFoundException extends NonRetryableIngestionException {

    public DocumentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
