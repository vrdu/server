package com.example.server.Orchestrator;

import com.example.server.entity.Document;
import com.example.server.manager.ExtractionManager;
import com.example.server.repository.DocumentRepository;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

@Component
public class PromptOrchestrator {
    private final ExtractionManager extractionManager;
    private final ExecutorService executor;
    private final DocumentRepository documentRepository;
    private final LLMOrchestrator llmOrchestrator;
    private final Semaphore semaphore = new Semaphore(4); // Control worker availability

    @Autowired
    public PromptOrchestrator(ExtractionManager extractionManager, DocumentRepository documentRepository, LLMOrchestrator llmOrchestrator) {
        this.extractionManager = extractionManager;
        this.documentRepository = documentRepository;
        this.llmOrchestrator = llmOrchestrator;
        this.executor = Executors.newFixedThreadPool(4);
    }

    public void startPromptGenerationOrchestration() {
        while (true) {
            try {
                Map<Triple<String, String, String>, List<String>> extraction = extractionManager.getOldestExtraction();

                if (extraction != null) {
                    promptOrchestrator(extraction, 5);
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
    private void promptOrchestrator(Map<Triple<String, String, String>, List<String>> extraction, int nrInstructions) {

        Triple<String, String, String> key = extraction.keySet().iterator().next();
        List<String> documentNames = extraction.get(key);
        Pageable pageable = PageRequest.of(0, nrInstructions);

        List<Document> instructionDocuments = documentRepository.findAllByProjectNameAndOwnerAndInstructionTrue(key.getMiddle(), key.getLeft(), pageable);

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

                    if (document.getStatus() == Document.Status.PENDING || document.getStatus() == Document.Status.PROMPT_GENERATION_IN_PROGRESS) {
                        if (document.isCurrentlyInOCR()) {
                            // Skip and retry fetching later
                            continue;
                        } else {
                            // Add to the current batch
                            currentBatch.add(document);
                            iterator.remove();
                        }
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
                            makePrompts(key.getLeft(), key.getMiddle(), batchToProcess, instructionDocuments);
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
        extractionManager.addToPromptingQueue(extraction);
        llmOrchestrator.startPromptingOrchestration();
    }
    private void makePrompts(String owner, String projectName, List<Document> extractionDocuments, List<Document> instructionDocuments) {
        String generatingPrompt = "";
        for (Document extractionDocument : extractionDocuments) {
            generatingPrompt += "This is the opening instruction\n";
            generatingPrompt += "Label part from instructionDocument[0]\n";
            generatingPrompt += "Now are a couple of instruciton documents following\n";
            for (Document instructionDocument : instructionDocuments) {
                generatingPrompt += "For this document:\n";
                generatingPrompt += instructionDocument.getOcrData();
                generatingPrompt += "\n";
                generatingPrompt += "This would be the solution\n";
                generatingPrompt += instructionDocument.getExtractionResult();
                generatingPrompt += "\n";
            }
            generatingPrompt += "This would be your document, where you have to extract the information\n";
            extractionDocument.getOcrData();
            extractionDocument.setPrompt(generatingPrompt);
            extractionDocument.setStatus(Document.Status.PROMPT_COMPLETE);
            documentRepository.save(extractionDocument);
        }
    }

}
