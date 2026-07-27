package com.cloudspring.cloudspring_rag_ingestion.exception;

public class EmptyDocumentException extends NonRetryableIngestionException {

    public EmptyDocumentException(String message) {
        super(message);
    }
}
