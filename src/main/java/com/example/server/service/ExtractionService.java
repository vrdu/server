package com.example.server.service;

import com.example.server.entity.Document;
import com.example.server.entity.Extraction;
import com.example.server.repository.DocumentRepository;
import com.example.server.repository.ExtractionRepository;

import java.util.ArrayList;
import java.util.List;

public class ExtractionService {

    private final ExtractionRepository extractionRepository;
    private final DocumentRepository documentRepository;

    public ExtractionService(ExtractionRepository extractionRepository, DocumentRepository documentRepository) {
        this.extractionRepository = extractionRepository;
        this.documentRepository = documentRepository;
    }


    public List <Document> getExtractionDocuments(String owner,String projectName) {
        List <Document> extractionDocuments = new ArrayList<>(documentRepository.findAllByProjectNameAndOwnerAndInstructionFalse(owner, projectName));
        return extractionDocuments;
    }
    public List <Extraction> getExtractions(String owner,String projectName){
        return extractionRepository.findAllByOwnerAndProjectName(owner,projectName);
    }
}
