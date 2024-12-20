package com.example.server.Orchestrator;

import com.example.server.entity.Document;
import com.example.server.entity.Label;
import com.example.server.entity.LabelFamily;
import com.example.server.manager.ExtractionManager;
import com.example.server.repository.DocumentRepository;
import com.example.server.repository.LabelFamilyRepository;
import com.example.server.repository.LabelRepository;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
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
    private final Semaphore semaphore = new Semaphore(4); // Control worker availability
    private final LabelFamilyRepository labelFamilyRepository;
    private final LabelRepository labelRepository;

    @Autowired
    public PromptOrchestrator(ExtractionManager extractionManager, DocumentRepository documentRepository, LLMOrchestrator llmOrchestrator, LabelFamilyRepository labelFamilyRepository, LabelRepository labelRepository) {
        this.extractionManager = extractionManager;
        this.documentRepository = documentRepository;
        this.labelFamilyRepository = labelFamilyRepository;
        this.labelRepository = labelRepository;
        this.executor = Executors.newFixedThreadPool(4);
    }

    @Async
    public void startPromptGenerationOrchestration() {
        System.out.println("prompt generation started");
        while (true) {
            try {
                Map<Triple<String, String, String>, List<String>> extraction = extractionManager.getOldestExtraction();

                if (extraction != null) {
                    System.out.println("extraction not null");
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
        //is set to max 5 instructions look startPromptGenerationOrchestration
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

    }
    private void makePrompts(String owner, String projectName, List<Document> extractionDocuments, List<Document> instructionDocuments) {
        List <LabelFamily> instructionFamilies = labelFamilyRepository.findAllByProjectNameAndOwner(projectName, owner);

        List <Label> labels = new ArrayList<>();
        for (LabelFamily instructionFamily : instructionFamilies) {
            labels.addAll(labelRepository.findAllByLabelFamilyId(instructionFamily.getId()));
        }
        String generatingPrompt = "";
        for (Document extractionDocument : extractionDocuments) {
            generatingPrompt += "This is the opening instruction\n";
            for (Label label : labels) {
                generatingPrompt += label.getLabelName() + "\n";
                generatingPrompt += label.getLabelDescription() + "\n";
            }
            generatingPrompt += "Now are a couple of instruction documents following\n";
            for (Document instructionDocument : instructionDocuments) {

                generatingPrompt += "For this document:\n";
                generatingPrompt += instructionDocument.getOcrData();
                generatingPrompt += "\n";
                generatingPrompt += "This would be the solution\n";
                System.out.println("middle"+instructionDocument.getAnnotations().toString());
                generatingPrompt += instructionDocument.getAnnotations().toString();
                generatingPrompt += "\n";

            }
            generatingPrompt += "This would be your document, where you have to extract the information\n";
            generatingPrompt += extractionDocument.getOcrData();
            extractionDocument.setPrompt(generatingPrompt);
            System.out.println("finished prompt:");
            System.out.println(generatingPrompt);
            extractionDocument.setStatus(Document.Status.PROMPT_COMPLETE);
            documentRepository.save(extractionDocument);
        }
    }

}
