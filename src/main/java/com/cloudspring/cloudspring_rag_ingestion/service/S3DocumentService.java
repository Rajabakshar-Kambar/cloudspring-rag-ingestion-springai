package com.cloudspring.cloudspring_rag_ingestion.service;

import com.cloudspring.cloudspring_rag_ingestion.exception.DocumentNotFoundException;
import com.cloudspring.cloudspring_rag_ingestion.exception.TransientIngestionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class S3DocumentService {

    private final S3Client s3Client;

    /**
     * Content length of the object, without downloading the body. Used to
     * reject oversized files before they're pulled into memory.
     */
    public long contentLength(
            String bucket,
            String key,
            String versionId) {

        try {

            return s3Client.headObject(
                            HeadObjectRequest.builder()
                                    .bucket(bucket)
                                    .key(key)
                                    .versionId(versionId)
                                    .build())
                    .contentLength();

        } catch (NoSuchKeyException e) {

            throw new DocumentNotFoundException(
                    "S3 object not found: s3://" + bucket + "/" + key
                            + " (versionId=" + versionId + ")",
                    e);

        } catch (SdkException e) {

            throw new TransientIngestionException(
                    "Failed to read metadata for s3://" + bucket + "/" + key,
                    e);
        }
    }

    /**
     * Downloads the object fully into memory and closes the underlying
     * connection. Returning byte[] instead of a live InputStream keeps
     * resource management (and retries) simple for callers.
     */
    public byte[] download(
            String bucket,
            String key,
            String versionId) {

        GetObjectRequest request =
                GetObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .versionId(versionId)
                        .build();

        try (ResponseInputStream<GetObjectResponse> response =
                     s3Client.getObject(request)) {

            return response.readAllBytes();

        } catch (NoSuchKeyException e) {

            throw new DocumentNotFoundException(
                    "S3 object not found: s3://" + bucket + "/" + key
                            + " (versionId=" + versionId + ")",
                    e);

        } catch (SdkException | IOException e) {

            throw new TransientIngestionException(
                    "Failed to download s3://" + bucket + "/" + key,
                    e);
        }
    }
}
