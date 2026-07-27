package com.cloudspring.cloudspring_rag_ingestion.service;

import com.cloudspring.cloudspring_rag_ingestion.exception.EmptyDocumentException;
import com.cloudspring.cloudspring_rag_ingestion.exception.NonRetryableIngestionException;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PdfExtractorService {

    /**
     * Extracts the full text of a PDF into a single Spring AI Document,
     * mirroring the old PDFTextStripper.getText(document) behaviour
     * (whole-document text, not split per page).
     */
    public Document extract(
            byte[] pdfBytes,
            String documentName) {

        List<Document> pages;

        try {

            PagePdfDocumentReader pdfReader =
                    new PagePdfDocumentReader(
                            new ByteArrayResource(pdfBytes),
                            PdfDocumentReaderConfig.builder()
                                    .withPageTopMargin(0)
                                    .withPageExtractedTextFormatter(
                                            ExtractedTextFormatter.builder().build())
                                    .withPagesPerDocument(1)
                                    .build());

            pages = pdfReader.read();

        } catch (RuntimeException e) {

            // A malformed/corrupt/encrypted PDF is a bad input, not a
            // transient infra failure - don't let SQS retry it forever.
            throw new NonRetryableIngestionException(
                    "Failed to parse PDF: " + documentName, e);
        }

        String fullText =
                pages.stream()
                        .map(Document::getText)
                        .filter(text -> text != null && !text.isBlank())
                        .collect(Collectors.joining("\n"));

        if (fullText.isBlank()) {

            throw new EmptyDocumentException(
                    "No extractable text in " + documentName
                            + " (scanned image PDF without OCR?)");
        }

        return Document.builder()
                .text(fullText)
                .metadata(Map.of("documentName", documentName))
                .build();
    }
}