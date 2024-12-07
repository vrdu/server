package com.example.server.service;

import com.example.server.entity.Annotation;
import com.example.server.entity.BoundingBox;
import com.example.server.entity.Document;
import com.example.server.repository.DocumentRepository;
import com.example.server.rest.dto.DocumentGetCompleteDTO;
import com.example.server.rest.dto.DocumentSetCompleteDTO;
import com.example.server.rest.dto.HighlightDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.Word;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import net.sourceforge.tess4j.ITessAPI;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

import java.io.IOException;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


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
        System.out.println("in safeInDB");
        boolean documentExists = checkDuplicates(document.getDocumentName(), document.getProjectName(), document.getOwner());
        if (!documentExists) {
            document.setCurrentlyInOCR(true);
            document = documentRepository.save(document);
            documentRepository.flush();
            System.out.println("safed document:{}" + document);
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
        System.out.println("Started OCR process for document: " + document.getDocumentName());
        Document ocrResult = performOCRForPrompt(document);
        ocrResult.setCurrentlyInOCR(false);
        Optional <Document> documentFromDBOpt = documentRepository.findByOwnerAndProjectNameAndDocumentName(ocrResult.getOwner(),ocrResult.getProjectName(),ocrResult.getDocumentName());
        if (documentFromDBOpt.isPresent()){
            documentRepository.save(ocrResult);
            documentRepository.flush();
        }

        return CompletableFuture.completedFuture(null);
    }

    public Document performOCRForPrompt(Document document) {
        log.debug("Started OCR process for document: {}", document.getDocumentName());

        if (!document.isOcrNotPossible()) {
            ITesseract tesseract = new Tesseract();
            /*tesseract.setDatapath("C:\\Program Files\\Tesseract-OCR\\tessdata");LAPTOP*/

            tesseract.setDatapath("E:\\program files\\tessdata");/* PC*/
            tesseract.setLanguage("eng");
            tesseract.setTessVariable("user_defined_dpi", "300");

            StringBuilder ocrResultBuilder = new StringBuilder();
            List<BoundingBox> boundingBoxes = new ArrayList<>();

            try (PDDocument pdfDocument = PDDocument.load(new ByteArrayInputStream(document.getPdfData()))) {
                PDFRenderer pdfRenderer = new PDFRenderer(pdfDocument);
                int pageCount = pdfDocument.getNumberOfPages();
                log.info("PDF has {} pages", pageCount);

                for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
                    BufferedImage image = pdfRenderer.renderImageWithDPI(pageIndex, 300);

                    int maxRetries = 3;
                    int attempt = 0;
                    boolean success = false;

                    while (attempt < maxRetries && !success) {
                        try {
                            // Perform OCR and get the full text with line and paragraph preservation
                            String result = tesseract.doOCR(image);
                            result = result.replaceAll("\\r\\n|\\r|\\n", "\n").replaceAll("(?m)^[ \\t]*\\n", "\n\n");

                            // Extract words and their bounding boxes
                            List<Word> words = tesseract.getWords(image, ITessAPI.TessPageIteratorLevel.RIL_WORD);
                            StringBuilder pageResult = new StringBuilder();

                            for (Word word : words) {
                                pageResult.append(word.getText()).append(" ");
                                BoundingBox box = new BoundingBox(word.getBoundingBox(), word.getText(), pageIndex);
                                boundingBoxes.add(box);
                            }

                            // Append the OCR result and preserve newlines for page, line, and paragraph breaks
                            ocrResultBuilder.append("Page ").append(pageIndex + 1).append(":\n")
                                    .append(result).append("\n\n");

                            log.info("OCR successful on page {} at attempt {}", pageIndex + 1, attempt + 1);
                            success = true;
                        } catch (Exception e) {
                            attempt++;
                            log.error("OCR failed on attempt {}: {}", attempt, e.getMessage());

                            if (attempt < maxRetries) {
                                log.info("Retrying OCR...");
                            } else {
                                log.error("Max retries reached. OCR failed.");
                                document.setOcrNotPossible(true);
                            }
                        }
                    }
                }

                // Store the complete OCR result and the bounding boxes
                document.setOcrData(ocrResultBuilder.toString());
                document.setOcrBoxes(boundingBoxes);

                return document;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return document;
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public void convertDocumentSetCompleteDTOToEntity(DocumentSetCompleteDTO documentSetCompleteDTO, String documentName, String projectName, String username) {

        // Fetch the existing document by unique constraints (documentName, projectName, owner)
        Optional<Document> existingDocumentOpt = documentRepository.findByOwnerAndProjectNameAndDocumentName(username, projectName, documentName);
        System.out.println("documentName: " + documentName + " projectName: " + projectName + " username: "+ username);
        if (existingDocumentOpt.isEmpty()) {
            throw new RuntimeException("Document not found for updating annotations.");
        }

        Document existingDocument = existingDocumentOpt.get();

        // Convert each highlight into an Annotation and update the existing document's annotations
        List<Annotation> annotations = documentSetCompleteDTO.getHighlights().stream()
                .map(highlight -> {
                    try {
                        String jsonData = objectMapper.writeValueAsString(highlight); // Serialize each highlight to JSON
                        Annotation annotation = new Annotation();
                        annotation.setAnnotationId(generateUniqueAnnotationId()); // Generate unique ID
                        annotation.setJsonData(jsonData);
                        return annotation;
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Failed to save annotations", e);
                    }
                })
                .collect(Collectors.toList());

        // Set the new annotations on the existing document
        existingDocument.setAnnotations(annotations);

        // Save the updated document
        documentRepository.save(existingDocument);
        documentRepository.flush();
    }

    public DocumentGetCompleteDTO convertEntityToDocumentGetCompleteDTO(Document document) {
        DocumentGetCompleteDTO dto = new DocumentGetCompleteDTO();
        dto.setName(document.getDocumentName());


        // Deserialize each Annotation's jsonData to a HighlightDTO
        List<HighlightDTO> highlights = document.getAnnotations().stream()
                .map(annotation -> {
                    try {
                        return objectMapper.readValue(annotation.getJsonData(), HighlightDTO.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Failed to get the annotations.");

                    }
                })
                .collect(Collectors.toList());

        dto.setHighlights(highlights);
        return dto;
    }

    private static int generateUniqueAnnotationId() {
        return (int) (Math.random() * 1_000_000);

    }

    public void setAsInstruction(String documentName, String projectName, String username) {
        Optional<Document> documentDBOpt = documentRepository.findByOwnerAndProjectNameAndDocumentName(username, projectName, documentName);
        if (documentDBOpt.isPresent()) {
            Document documentDB = documentDBOpt.get();
            documentDB.setInstruction(true);
            documentRepository.save(documentDB);
            documentRepository.flush();
        } else {
            throw new RuntimeException("Document not found for setting as instruction.");
        }
    }

    /* First approach:
    public void setAnnotations(Document document, DocumentSetCompleteDTO documentSetCompleteDTO){
        boolean documentExists = checkDuplicates(document.getDocumentName(), document.getProjectName(), document.getOwner());
        if (documentExists) {
            List <String> stringAnnotations = documentSetCompleteDTO.getAnnotation();
            List <Annotation> listAnnotations = new ArrayList<>();
            for (String annotationString : stringAnnotations){
                Annotation annotation = new Annotation();
                annotation.setJsonData(annotationString);
                listAnnotations.add(annotation);
            }
            document.setAnnotations(listAnnotations);
            documentRepository.save(document);
            documentRepository.flush();
        } else {
            safeInDB(document);
            setAnnotations(document, documentSetCompleteDTO);
        }
    }
    public List <String> getAnnotations(List <Annotation> annotationsToParse){
        List <String> annotationsAsString = new ArrayList<>();
        for (Annotation annotation : annotationsToParse){
            annotationsAsString.add(annotation.getJsonData());
        }
        return  annotationsAsString;
    }

    private  static String mapJsonToString(Object json) {
        try {
            return objectMapper.writeValueAsString(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert JSON to string", e);
        }
    }
    private static JsonNode mapStringToJson(String jsonString) {
        try {
            return objectMapper.readTree(jsonString);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert string to JSON", e);
        }
    }
*/
/* with correct boxes implementation
    public Document performOCRForPrompt(Document document) {
        log.debug("Started OCR process for document: {}", document.getDocumentName());

        if (!document.isOcrNotPossible()) {
            ITesseract tesseract = new Tesseract();
            tesseract.setDatapath("C:\\Program Files\\Tesseract-OCR\\tessdata");
            tesseract.setLanguage("deu");
            tesseract.setTessVariable("user_defined_dpi", "300");

            StringBuilder ocrResultBuilder = new StringBuilder();
            List<BoundingBox> boundingBoxes = new ArrayList<>();

            try (PDDocument pdfDocument = PDDocument.load(new ByteArrayInputStream(document.getPdfData()))) {
                PDFRenderer pdfRenderer = new PDFRenderer(pdfDocument);
                int pageCount = pdfDocument.getNumberOfPages();
                log.info("PDF has {} pages", pageCount);

                for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
                    BufferedImage image = pdfRenderer.renderImageWithDPI(pageIndex, 300);

                    int maxRetries = 3;
                    int attempt = 0;
                    boolean success = false;

                    while (attempt < maxRetries && !success) {
                        try {
                            List<Word> words = tesseract.getWords(image, ITessAPI.TessPageIteratorLevel.RIL_WORD);
                            StringBuilder pageResult = new StringBuilder();

                            for (Word word : words) {
                                pageResult.append(word.getText()).append(" ");
                                BoundingBox box = new BoundingBox(word.getBoundingBox(), word.getText());
                                boundingBoxes.add(box);
                            }

                            pageResult.append("\n\n");

                            ocrResultBuilder.append("Page ").append(pageIndex + 1).append(":\n")
                                    .append(pageResult.toString()).append("\n");

                            log.info("OCR successful on page {} at attempt {}", pageIndex + 1, attempt + 1);
                            success = true;
                        } catch (Exception e) {
                            attempt++;
                            log.error("OCR failed on attempt {}: {}", attempt, e.getMessage());

                            if (attempt < maxRetries) {
                                log.info("Retrying OCR...");
                            } else {
                                log.error("Max retries reached. OCR failed.");
                                document.setOcrNotPossible(true);
                            }
                        }
                    }
                }

                document.setOcrData(ocrResultBuilder.toString());
                document.setOcrBoxes(boundingBoxes);

                return document;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return document;
    }

*/
/*
    public Document performOCRForPrompt(Document document) {
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

                            result = result.replaceAll("\\r\\n|\\r|\\n", "\n").replaceAll("(?m)^[ \\t]*\\n", "\n\n");

                            log.info("OCR successful on page {} at attempt {}", pageIndex + 1, attempt + 1);


                            // Append the OCR result for the current page to the StringBuilder
                            ocrResultBuilder.append("Page ").append(pageIndex + 1).append(":\n")
                                    .append(result).append("\n\n");  // Preserving newlines

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

                // Store the complete OCR result with preserved formatting
                document.setOcrData(ocrResultBuilder.toString());


                return document;

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return document;
    }
*/
}

