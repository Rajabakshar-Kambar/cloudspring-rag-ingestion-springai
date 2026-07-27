package com.cloudspring.cloudspring_rag_ingestion.model;

public record DocumentMessage(
        String bucketName,
        String objectKey,
        String versionId
) {
}