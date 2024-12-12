package com.example.server.manager;


import java.util.*;

import com.example.server.entity.Extraction;
import com.example.server.entity.SingleExtraction;
import com.example.server.repository.ExtractionRepository;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

import static com.example.server.entity.Extraction.Status.*;

@Component
public class ExtractionManager {

    private final Queue<Map<Triple<String, String, String>,List<String>>> promptGenerationQueue;
    private final Queue<Map<Triple<String, String, String>, List<String>>> promptingQueue;
    private final ExtractionRepository extractionRepository;


    @Autowired
    private ExtractionManager(@Qualifier("promptingQueue")Queue<Map<Triple<String, String, String>, List<String>>> promptingQueue,
                              @Qualifier("promptGenerationQueue")Queue<Map<Triple<String, String, String>, List<String>>> promptGenerationQueue,
                              ExtractionRepository extractionRepository) {
        this.promptingQueue = promptingQueue;
        this.promptGenerationQueue = promptGenerationQueue;
        this.extractionRepository = extractionRepository;

        loadUnfinishedExtractions();
        loadPendingExtractions();

    }

    private void loadUnfinishedExtractions() {
        List<Extraction> unfinishedExtractions = extractionRepository.findByStatusIn(List.of(PENDING, PROMPT_GENERATION_IN_PROGRESS));

        for (Extraction extraction : unfinishedExtractions) {
            Triple<String, String, String> key = Triple.of(
                    extraction.getOwner(),
                    extraction.getProjectName(),
                    extraction.getId().toString()
            );
            List<String> extractionNames = new ArrayList<>();
            for (SingleExtraction singleExtraction : extraction.getExtractions()) {
                extractionNames.add(singleExtraction.getExtractionDocumentName());
            }
            Map<Triple<String, String, String>, List<String>> extractionMap = new ConcurrentHashMap<>();
            extractionMap.put(key, extractionNames);

            synchronized (this) {
                promptGenerationQueue.add(extractionMap);
            }
        }
    }


    public synchronized void addExtraction(String owner, String projectName, String ExtractionId, List<String> documentNames) {
        Triple<String, String, String> key = Triple.of(owner, projectName, ExtractionId);

        if (promptGenerationQueue.contains(key)) {
            throw new IllegalStateException("Extraction already in progress for: " + key);
        }
        Map<Triple<String, String, String>, List<String>> extraction= new ConcurrentHashMap<>();
        extraction.put(key, documentNames);

        promptGenerationQueue.add(extraction);
    }

    public synchronized Map<Triple<String, String, String>, List<String>> getOldestExtraction() {
        return promptGenerationQueue.poll(); // Remove and return the first map in the queue
    }

    //if crash, then add all the not finished prompted prompts back to the queue
    private void loadPendingExtractions() {
        List<Extraction> pendingExtractions = extractionRepository.findByStatusIn(List.of(PROMPT_COMPLETE, EXTRACTION_IN_PROGRESS));

        for (Extraction extraction : pendingExtractions) {
            Triple<String, String, String> key = Triple.of(
                    extraction.getOwner(),
                    extraction.getProjectName(),
                    extraction.getId().toString()
            );
            List<String> extractionNames = new ArrayList<>();
            for (SingleExtraction singleExtraction : extraction.getExtractions()) {
                extractionNames.add(singleExtraction.getExtractionDocumentName());
            }
            Map<Triple<String, String, String>, List<String>> extractionMap = new ConcurrentHashMap<>();
            extractionMap.put(key, extractionNames);

            synchronized (this) {
                promptingQueue.add(extractionMap);
            }
        }
    }

    public synchronized void addToPromptingQueue(Map<Triple<String, String, String>, List<String>> extractionMap) {
        Triple<String, String, String> key = extractionMap.keySet().iterator().next();

        if (promptingQueue.stream().anyMatch(map -> map.containsKey(key))) {
            throw new IllegalStateException("Extraction already exists in prompting queue: " + key);
        }

        promptingQueue.add(extractionMap);
    }

    public synchronized Map<Triple<String, String, String>, List<String>> getNextPromptingExtraction() {
        return promptingQueue.poll();
    }


}
