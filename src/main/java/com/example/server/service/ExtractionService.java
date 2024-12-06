package com.example.server.service;

import com.example.server.Orchestrator.LLMOrchestrator;
import com.example.server.Orchestrator.PromptOrchestrator;
import com.example.server.entity.Document;
import com.example.server.entity.Extraction;
import com.example.server.entity.SingleExtraction;
import com.example.server.manager.ExtractionManager;
import com.example.server.repository.DocumentRepository;
import com.example.server.repository.ExtractionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional
public class ExtractionService {

    private final ExtractionRepository extractionRepository;
    private final DocumentRepository documentRepository;
    private final ExtractionManager extractionManager;
    private final LLMOrchestrator llmOrchestrator;
    private final PromptOrchestrator promptOrchestrator;

    @Autowired
    public ExtractionService(ExtractionRepository extractionRepository, DocumentRepository documentRepository, ExtractionManager extractionManager, LLMOrchestrator llmOrchestrator, PromptOrchestrator promptOrchestrator) {
        this.extractionRepository = extractionRepository;
        this.documentRepository = documentRepository;
        this.extractionManager = extractionManager;
        this.llmOrchestrator = llmOrchestrator;
        this.promptOrchestrator = promptOrchestrator;
    }


    public void addExtraction(Extraction extraction) {
        if (extraction.getExtractionName() == null || extraction.getExtractionName().isEmpty()) {
            extraction.setExtractionName(getRandomExtractionName());
        }
        extraction.setStatus(Extraction.Status.PENDING);
        extractionRepository.save(extraction);
        extractionRepository.flush();

        Extraction extractionFromDB;
        Optional<Extraction> extractionFromDBOpt = extractionRepository.findByOwnerAndProjectNameAndExtractionName(extraction.getOwner(), extraction.getProjectName(), extraction.getExtractionName());

        if (extractionFromDBOpt.isPresent()) {
            extractionFromDB = extractionFromDBOpt.get();
            extractionFromDB.setStatus(Extraction.Status.PROMPT_GENERATION_IN_PROGRESS);
            List<String> extractionNames = new ArrayList<>();
            for (SingleExtraction singleExtraction : extraction.getExtractions()) {
                extractionNames.add(singleExtraction.getExtractionName());
            }
            extractionManager.addExtraction(extraction.getOwner(), extraction.getProjectName(), extraction.getExtractionName(), extractionNames);
            extractionRepository.save(extractionFromDB);
            extractionRepository.flush();
            CompletableFuture.runAsync(() -> {
                promptOrchestrator.startPromptGenerationOrchestration();
            });

            CompletableFuture.runAsync(() -> {
                llmOrchestrator.startPromptingOrchestration();
            });
        }
    }
    public  Extraction getExtraction(String owner,String projectName, String extractionName){
        Optional<Extraction> extraction = extractionRepository.findByOwnerAndProjectNameAndExtractionName(owner,projectName, extractionName);
        if (extraction.isPresent()){
            return extraction.get();
        }
        throw new RuntimeException("Extraction not found");
    }
    public List <String> extractExtractionDocuments(List <SingleExtraction> extractions){
        List <String> documentNames = new ArrayList<>();
        for (SingleExtraction singleExtraction : extractions){
                documentNames.add(singleExtraction.getExtractionName());
            }
        return documentNames;
    }

    public List <Document> getExtractionDocuments(String owner,String projectName) {
        List <Document> extractionDocuments = new ArrayList<>(documentRepository.findAllByProjectNameAndOwnerAndInstructionFalse(owner, projectName));
        return extractionDocuments;
    }
    public List <Extraction> getExtractions(String owner,String projectName){
        return extractionRepository.findAllByOwnerAndProjectName(owner,projectName);
    }
    private String getRandomExtractionName(){
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder randomName = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 10; i++) {
            int index = random.nextInt(characters.length());
            randomName.append(characters.charAt(index));
        }

        return randomName.toString();
    }
}
