package com.example.server.Orchestrator;

import com.example.server.entity.Document;
import com.example.server.manager.ExtractionManager;
import com.example.server.repository.DocumentRepository;
import org.apache.commons.lang3.tuple.Triple;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PromptOrchestrator {
    private final ExtractionManager extractionManager;
    private final Map<Triple<String, String, String>, List<String>> prompts;
    private final ExecutorService executor;
    private final DocumentRepository documentRepository;

    public PromptOrchestrator(ExtractionManager extractionManager, DocumentRepository documentRepository) {
        this.extractionManager = extractionManager;
        this.documentRepository = documentRepository;
        this.prompts = new HashMap<>();
        this.executor = Executors.newFixedThreadPool(4);
    }

    public void startOrchestration() {
        while (true) {
            try {
                Map<Triple<String, String, String>, List<String>> extraction = extractionManager.getOldestExtraction();

                if (extraction != null) {
                    processExtraction(extraction);
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
    private void processExtraction(Map<Triple<String, String, String>, List<String>> extraction) {
        Triple<String, String, String> key = extraction.keySet().iterator().next();
        List<String> documentNames = extraction.get(key);

        List<String> readyDocuments = new ArrayList<>();
        List<String> pendingDocuments = new ArrayList<>();
        Document document;
        for (String documentName : documentNames){
            Optional<Document> documentOpt = documentRepository.findByOwnerAndProjectNameAndDocumentName(key.getLeft(), key.getMiddle(), documentName);


        }
    }




}
