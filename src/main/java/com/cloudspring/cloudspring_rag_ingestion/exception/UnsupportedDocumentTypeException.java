package com.cloudspring.cloudspring_rag_ingestion.exception;

public class UnsupportedDocumentTypeException extends NonRetryableIngestionException {

    public UnsupportedDocumentTypeException(String message) {
        super(message);
    }
}
