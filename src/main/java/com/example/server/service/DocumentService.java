package com.example.server.service;

import com.example.server.entity.Document;
import com.example.server.repository.DocumentRepository;
import jakarta.transaction.Transactional;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
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
import java.util.List;
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
            document.setCurrentlyInOCR(true);
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

    public List<Document> getDocuments(String username, String  projectName){
        List <Document> documentsDB = documentRepository.findAllByProjectNameAndOwner(projectName, username);
        return documentsDB;
    }
    public Document getAnnotationDocuments(String username, String projectName, String documentName){
        Optional <Document> documentDBOpt = documentRepository.findByOwnerAndProjectNameAndDocumentName(username,projectName,documentName);
        if (documentDBOpt.isPresent()){
            Document documentDB = documentDBOpt.get();
            if(!documentDB.isCurrentlyInOCR()){
                if (!documentDB.isOcrNotPossible()){
                    if(!documentDB.getOcrData().isEmpty()){
                        return documentDB;
                    }else{
                        throw new RuntimeException("You are to fast, OCR did not even start. Please wait until it is finished.");
                    }
                }else{
                    throw new RuntimeException("OCR is not possible. Please try to delete and upload it again.");
                }
            }else{
                throw new RuntimeException("Wait until the OCR is finished.");

            }
        }else{
            throw new RuntimeException("Something went wrong the document does not exist.");
        }
    }
    public void deleteDocument(Document documentToDelete){
        System.out.println("DocumentName: " + documentToDelete.getDocumentName());
        System.out.println("Owner: "+ documentToDelete.getOwner());
        System.out.println("ProjectName: "+ documentToDelete.getProjectName());
        Optional <Document> documentToDeleteDBOpt = documentRepository.findByOwnerAndProjectNameAndDocumentName(documentToDelete.getOwner(),
                                                                                                                documentToDelete.getProjectName(),
                                                                                                                documentToDelete.getDocumentName());
        if (documentToDeleteDBOpt.isPresent()){
            Document documentToDeleteDB = documentToDeleteDBOpt.get();
            documentRepository.delete(documentToDeleteDB);
        }else{
            throw new RuntimeException("This document could not be deleted, because it did not exist.");

        }
    }
    @Async
    public CompletableFuture<Void> startOCRProcessAsync(Document document) {
        Document ocrResult = performOCR(document);
        ocrResult.setCurrentlyInOCR(false);
        Optional <Document> documentFromDBOpt = documentRepository.findByOwnerAndProjectNameAndDocumentName(ocrResult.getOwner(),ocrResult.getProjectName(),ocrResult.getDocumentName());
        if (documentFromDBOpt.isPresent()){
            documentRepository.save(ocrResult);
            documentRepository.flush();
        }

        return CompletableFuture.completedFuture(null);
    }

    public Document performOCR(Document document) {
        log.debug("Started OCR process for document: {}", document.getDocumentName());
        if (!document.isOcrNotPossible()) {  // Simplified boolean check
            ITesseract tesseract = new Tesseract();
            tesseract.setDatapath("C:\\Program Files\\Tesseract-OCR\\tessdata");  // Set the Tesseract data path
            tesseract.setLanguage("deu");  // Set the language to German
            tesseract.setTessVariable("user_defined_dpi", "300");

            // Use StringBuilder to accumulate OCR results from multiple pages
            StringBuilder ocrResultBuilder = new StringBuilder();

            // Convert byte[] (PDF data) to BufferedImage(s) using PDFBox
            try (PDDocument pdfDocument = PDDocument.load(new ByteArrayInputStream(document.getPdfData()))) {
                PDFRenderer pdfRenderer = new PDFRenderer(pdfDocument);
                int pageCount = pdfDocument.getNumberOfPages();
                log.info("PDF has {} pages", pageCount);

                for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
                    BufferedImage image = pdfRenderer.renderImageWithDPI(pageIndex, 300);

                    // Retry OCR process up to 3 times
                    int maxRetries = 3;
                    int attempt = 0;
                    boolean success = false;

                    while (attempt < maxRetries && !success) {
                        try {
                            // Perform OCR on the image
                            String result = tesseract.doOCR(image);
                            log.info("OCR successful on page {} at attempt {}", pageIndex + 1, attempt + 1);

                            // Append the OCR result for the current page to the StringBuilder
                            ocrResultBuilder.append("Page ").append(pageIndex + 1).append(":\n").append(result).append("\n");
                            document.setOcrData(ocrResultBuilder.toString());
                            success = true;  // OCR succeeded for this page

                        } catch (TesseractException e) {
                            attempt++;
                            log.error("OCR failed on attempt {}: {}", attempt, e.getMessage());

                            // Retry logic
                            if (attempt < maxRetries) {
                                log.info("Retrying OCR...");
                            } else {
                                log.error("Max retries reached. OCR failed.");
                                document.setOcrNotPossible(true);
                            }
                        }
                    }
                }

                return document;
            } catch (IOException e) {

                throw new RuntimeException(e);
            }


        }
        return document;
    }
}

