package com.cloudspring.cloudspring_rag_ingestion.service;

import com.cloudspring.cloudspring_rag_ingestion.config.IngestionProperties;
import com.cloudspring.cloudspring_rag_ingestion.exception.DocumentTooLargeException;
import com.cloudspring.cloudspring_rag_ingestion.exception.UnsupportedDocumentTypeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentValidationService {

    private final IngestionProperties ingestionProperties;
    private final S3DocumentService s3DocumentService;

    /**
     * Validates extension and size before a single byte is downloaded.
     * Throws a {@link com.cloudspring.cloudspring_rag_ingestion.exception.NonRetryableIngestionException}
     * subtype - the caller should not retry these, just log and move on.
     */
    public void validate(
            String bucketName,
            String objectKey,
            String versionId) {

        String extension = extensionOf(objectKey);

        if (!ingestionProperties.isSupportedExtension(extension)) {

            throw new UnsupportedDocumentTypeException(
                    "Unsupported file extension '" + extension + "' for "
                            + objectKey + ". Supported: "
                            + ingestionProperties.getSupportedExtensions());
        }

        long contentLength =
                s3DocumentService.contentLength(
                        bucketName,
                        objectKey,
                        versionId);

        long maxBytes = ingestionProperties.maxFileSizeBytes();

        if (contentLength > maxBytes) {

            throw new DocumentTooLargeException(
                    objectKey + " is " + contentLength
                            + " bytes, exceeding the "
                            + ingestionProperties.getMaxFileSizeMb()
                            + " MB limit");
        }
    }

    private String extensionOf(String objectKey) {

        int dotIndex = objectKey.lastIndexOf('.');

        return dotIndex == -1
                ? ""
                : objectKey.substring(dotIndex + 1);
    }
}
