package com.example.server.Orchestrator;

import com.example.server.controller.LLMController;
import com.example.server.entity.Document;
import com.example.server.manager.ExtractionManager;
import com.example.server.repository.DocumentRepository;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;


import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;


@Component
public class LLMOrchestrator {

    private final ExtractionManager extractionManager;
    private final ExecutorService executor;
    private final DocumentRepository documentRepository;
    private final Semaphore semaphore = new Semaphore(8); // Control worker availability
    private final LLMController llmController = new LLMController();

    @Autowired
    public LLMOrchestrator(ExtractionManager extractionManager, DocumentRepository documentRepository) {
        this.extractionManager = extractionManager;
        this.documentRepository = documentRepository;
        this.executor = Executors.newFixedThreadPool(4);
    }
    @Async
    public void startPromptingOrchestration() {
        System.out.println("prompt orchestration started");
        while (true) {
            try {
                Map<Triple<String, String, String>, List<String>> promptMap = extractionManager.getNextPromptingExtraction();

                if (promptMap != null) {
                    promptingOrchestrator(promptMap);
                } else {
                    // No extractions available, wait and retry
                    Thread.sleep(10000); // 10 seconds
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Orchestrator interrupted: " + e.getMessage());
                break;
            }
        }
    }

    private void promptingOrchestrator(Map<Triple<String, String, String>, List<String>> extraction) {

        Triple<String, String, String> key = extraction.keySet().iterator().next();
        List<String> documentNames = extraction.get(key);

        List<Document> currentBatch = new ArrayList<>();
        List<String> pendingDocuments = new ArrayList<>(documentNames);

        while (!pendingDocuments.isEmpty()) {
            Iterator<String> iterator = pendingDocuments.iterator();

            while (iterator.hasNext() && currentBatch.size() < 5) {
                String documentName = iterator.next();

                // Fetch the document from the database
                Optional<Document> documentOpt = documentRepository.findByOwnerAndProjectNameAndDocumentName(
                        key.getLeft(), key.getMiddle(), documentName);

                if (documentOpt.isPresent()) {
                    Document document = documentOpt.get();

                    if (document.getStatus() == Document.Status.PROMPT_COMPLETE || document.getStatus() == Document.Status.EXTRACTION_IN_PROGRESS) {

                            // Add to the current batch
                            currentBatch.add(document);
                            iterator.remove();

                    } else {
                        // Already processed, remove from pendingDocuments
                        iterator.remove();
                    }
                } else {
                    // Document not found, remove from pendingDocuments
                    iterator.remove();
                }
            }
            if (!currentBatch.isEmpty()) {
                // Wait for a worker to become available
                try {
                    semaphore.acquire();
                    List<Document> batchToProcess = new ArrayList<>(currentBatch);
                    executor.submit(() -> {
                        try {
                            try {
                                promptLLM(batchToProcess);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        } finally {
                            semaphore.release(); // Release worker after processing
                        }
                    });
                    currentBatch.clear(); // Clear current batch
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Error acquiring semaphore: " + e.getMessage());
                }
            }
            // If there are still pending documents, wait for a while
            if (!pendingDocuments.isEmpty() && currentBatch.isEmpty()) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Interrupted while waiting: " + e.getMessage());
                }
            }
        }
    }
    private void promptLLM(List <Document> prompts) throws IOException, InterruptedException {
        String projectId = "key";
        String region = "europe-west6";
        int maxRetries = 5;

        for (Document promptDocument : prompts) {
            promptDocument.setStatus(Document.Status.PROMPT_GENERATION_IN_PROGRESS);
            documentRepository.save(promptDocument);
            documentRepository.flush();

            boolean success = false;
            int attempts = 0;

            while (!success && attempts < maxRetries) {
                try {
                    attempts++;
                    GenerateContentResponse responseEntity = llmController.generateContent(promptDocument.getPrompt(), projectId, region);
                    promptDocument.setPrompt(responseEntity.toString());
                    promptDocument.setStatus(Document.Status.PROMPT_COMPLETE);
                    documentRepository.save(promptDocument);
                    documentRepository.flush();
                    success = true; // Mark success if no exception occurs
                } catch (Exception e) {
                    if (attempts >= maxRetries) {
                        promptDocument.setStatus(Document.Status.FAILED);
                        documentRepository.save(promptDocument);
                        documentRepository.flush();
                        System.err.println("Failed to generate prompt after " + maxRetries + " attempts for document: " + promptDocument.getId());
                    } else {
                        System.err.println("Retrying... (" + attempts + "/" + maxRetries + ")");
                        wait(1000);
                    }
                }
            }
        }
    }
}
