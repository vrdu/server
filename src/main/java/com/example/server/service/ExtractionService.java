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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional
public class ExtractionService {

    private final ExtractionRepository extractionRepository;
    private final DocumentRepository documentRepository;
    private final DocumentService documentService;
    private final ExtractionManager extractionManager;
    private final LLMOrchestrator llmOrchestrator;
    private final PromptOrchestrator promptOrchestrator;

    @Autowired
    public ExtractionService(ExtractionRepository extractionRepository, DocumentRepository documentRepository, DocumentService documentService, ExtractionManager extractionManager, LLMOrchestrator llmOrchestrator, PromptOrchestrator promptOrchestrator) {
        this.extractionRepository = extractionRepository;
        this.documentRepository = documentRepository;
        this.documentService = documentService;
        this.extractionManager = extractionManager;
        this.llmOrchestrator = llmOrchestrator;
        this.promptOrchestrator = promptOrchestrator;
    }


    public void addExtraction(Extraction extraction) {
        if (extraction.getExtractionName() == null || extraction.getExtractionName().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No extraction name was sent.");
        }

        extraction.setStatus(Extraction.Status.PENDING);

        if (extraction.getExtractions() != null) {
            for (SingleExtraction singleExtraction : extraction.getExtractions()) {
                singleExtraction.setExtraction(extraction);
            }
        }

        extractionRepository.save(extraction);
        extractionRepository.flush();

        Extraction extractionFromDB;
        Optional<Extraction> extractionFromDBOpt = extractionRepository.findByOwnerAndProjectNameAndExtractionName(extraction.getOwner(), extraction.getProjectName(), extraction.getExtractionName());

        if (extractionFromDBOpt.isPresent()) {
            extractionFromDB = extractionFromDBOpt.get();
            extractionFromDB.setStatus(Extraction.Status.PROMPT_GENERATION_IN_PROGRESS);
            List<String> extractionNames = new ArrayList<>();
            for (SingleExtraction singleExtraction : extraction.getExtractions()) {
                extractionNames.add(singleExtraction.getExtractionDocumentName());
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
                documentNames.add(singleExtraction.getExtractionDocumentName());
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

    public void calculateF1ForExtraction(String owner, String projectName, String extractionName) throws Exception {
        List<Double> f1Scores = new ArrayList<>();
        Optional<Extraction> extractionOpt = extractionRepository.findByOwnerAndProjectNameAndExtractionName(owner, projectName, extractionName);
        if (extractionOpt.isPresent()) {
            Extraction extraction = extractionOpt.get();
            List<SingleExtraction> singleExtractions = extraction.getExtractions();
            for (SingleExtraction singleExtraction : singleExtractions) {
                Optional<Document> documentOpt = documentRepository.findByOwnerAndProjectNameAndDocumentName(owner, projectName, singleExtraction.getExtractionDocumentName());
                if (documentOpt.isPresent()) {
                    Document document = documentOpt.get();
                    if (document.getF1()==null) {
                        double f1 = documentService.calculateF1Score(document.getExtractionResult(), document.getExtractionSolution());
                        document.setF1(f1);
                    }
                    f1Scores.add(document.getF1());
                    documentRepository.save(document);
                }
            }
            double averageF1 = f1Scores.stream().mapToDouble(val -> val).average().orElse(0.0);
            extraction.setF1(averageF1);
            extractionRepository.save(extraction);
        }
    }

}
