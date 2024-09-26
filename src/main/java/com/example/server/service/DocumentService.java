package com.example.server.service;

import com.example.server.entity.Document;
import com.example.server.repository.DocumentRepository;
import jakarta.transaction.Transactional;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import javax.print.Doc;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional
public class DocumentService {
    private final Logger log = LoggerFactory.getLogger(UserService.class);
    private final DocumentRepository documentRepository;

    @Autowired
    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @Async
    public CompletableFuture<Void> safeInDB(Document document) {
        boolean documentExists = checkDuplicates(document.getDocumentName(), document.getProjectName(), document.getOwner());
        if (!documentExists) {
            document = documentRepository.save(document);
            documentRepository.flush();
            log.debug("Created Project:{}", document);
        } else {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Document already exists in the project");
        }
        return CompletableFuture.completedFuture(null);
    }

    private boolean checkDuplicates(String documentName, String projectName, String owner) {
        Optional<Document> document = documentRepository.findByOwnerAndProjectNameAndDocumentName(owner, projectName, documentName);

        if (document.isPresent()) {
            return true;
        } else {
            return false;
        }


    }

    @Async
    public CompletableFuture<Void> startOCRProcessAsync(Document document) {
        Document ocrResult = performOCR(document);

        documentRepository.save(ocrResult);
        documentRepository.flush();
        log.debug("OCR process completed for document: {}", document.getDocumentName());

        return CompletableFuture.completedFuture(null);
    }

    public Document performOCR(Document document) {

        if (document.isOcrNotPossible() != true) {
            ITesseract tesseract = new Tesseract();
            tesseract.setDatapath("C:\\Program Files\\Tesseract-OCR\\tessdata");

            tesseract.setLanguage("de");

            int maxRetries = 3;
            int attempt = 0;
            boolean success = false;

            while (attempt < maxRetries && !success) {
                try {
                    // Convert the byte[] (PDF data) to a BufferedImage
                    BufferedImage image = ImageIO.read(new ByteArrayInputStream(document.getPdfData()));
                    if (image == null) {
                        throw new IOException("Failed to convert document to an image");
                    }

                    // Perform OCR on the image
                    String result = tesseract.doOCR(image);
                    document.setOcrData(result);
                    document.setOcrNotPossible(false);

                } catch (TesseractException | IOException e) {
                    attempt++;
                    log.error("OCR failed on attempt {}: {}", attempt, e.getMessage());  // Use logger for error

                    // Optionally add some delay before retrying
                    if (attempt < maxRetries) {
                        log.info("Retrying...");  // Use logger
                    } else {
                        log.error("Max retries reached. OCR failed.");
                        document.setOcrNotPossible(true);

                    }
                }

            }


        }
        return document;
    }

}





