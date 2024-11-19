package com.example.server.controller;

import com.example.server.entity.Document;
import com.example.server.entity.Extraction;
import com.example.server.rest.dto.DocumentAndExtractionDTO;
import com.example.server.rest.dto.DocumentGetCompleteDTO;
import com.example.server.rest.dto.DocumentGetDTO;
import com.example.server.rest.dto.ExtractionGetDTO;
import com.example.server.rest.mapper.DTOMapper;
import com.example.server.service.DocumentService;
import com.example.server.service.ExtractionService;
import com.example.server.service.ProjectService;
import com.example.server.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class ExtractionController {
    private final ExtractionService extractionService;
    private final UserService userService;

    public ExtractionController(ExtractionService extractionService, UserService userService) {

        this.extractionService = extractionService;
        this.userService = userService;
    }

    @GetMapping("/projects/{username}/{projectName}/{documentName}/documentsAndExtractions")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<DocumentAndExtractionDTO> getDocumentsAndExtractions(
            @PathVariable String documentName,
            @PathVariable String projectName,
            @PathVariable String username,
            HttpServletRequest request) throws IOException {

        userService.validateToken(request);
        List<Document> extractionDocuments = extractionService.getExtractionDocuments(username, projectName);
        List <Extraction> extractions = extractionService.getExtractions(username, projectName);
        List <DocumentGetDTO> documentGetDTOS = new ArrayList<>();
        List <ExtractionGetDTO> extractionGetDTOS = new ArrayList<>();
        for ( Document document : extractionDocuments){
            documentGetDTOS.add(DTOMapper.INSTANCE.convertEntityToDocumentGetDTO(document));
        }
        for ( Extraction extraction : extractions){
            extractionGetDTOS.add(DTOMapper.INSTANCE.convertEntityToExtractionGetDTO(extraction));
        }
        //DocumentAndExtractionDTO documentAndExtractionDTO =
        DocumentAndExtractionDTO documentAndExtractionDTO = new DocumentAndExtractionDTO();
        documentAndExtractionDTO.setExtractions(extractionGetDTOS);
        documentAndExtractionDTO.setDocuments(documentGetDTOS);
        return ResponseEntity.ok(documentAndExtractionDTO);
    }

    @GetMapping("/projects/{username}/{projectName}/{documentName}/oneExtraction")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<String> getOneExtraction(
            @PathVariable String documentName,
            @PathVariable String projectName,
            @PathVariable String username,
            HttpServletRequest request) throws IOException {

        userService.validateToken(request);/*
        List<Document> extractionDocuments = extractionService.getExtractionDocuments(username, projectName, documentName);
        List <Extraction> extractions = extractionService.getExtractions(username, projectName);
        List <DocumentGetDTO> documentGetDTOS = new ArrayList<>();
        List <ExtractionGetDTO> extractionGetDTOS = new ArrayList<>();
        for ( Document document : extractionDocuments){
            documentGetDTOS.add(DTOMapper.INSTANCE.convertEntityToDocumentGetDTO(document));
        }
        for ( Extraction extraction : extractions){
            documentGetDTOS.add(DTOMapper.INSTANCE.convertEntityToExtractionGetDTO(extraction));
        }
        //DocumentAndExtractionDTO documentAndExtractionDTO =
*/
        return ResponseEntity.ok("s");
    }
}
