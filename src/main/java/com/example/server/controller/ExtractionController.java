package com.example.server.controller;

import com.example.server.entity.Document;
import com.example.server.entity.Extraction;
import com.example.server.entity.SingleExtraction;
import com.example.server.rest.dto.*;
import com.example.server.rest.mapper.DTOMapper;
import com.example.server.service.ExtractionService;
import com.example.server.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@RestController
public class ExtractionController {
    private final ExtractionService extractionService;
    private final UserService userService;

    public ExtractionController(ExtractionService extractionService, UserService userService) {

        this.extractionService = extractionService;
        this.userService = userService;
    }

    @GetMapping("/projects/{username}/{projectName}/documentsAndExtractions")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<DocumentAndExtractionDTO> getDocumentsAndExtractions(
            @PathVariable String username,
            @PathVariable String projectName,
            HttpServletRequest request) throws IOException {
        try {
            System.out.println("documents and extractions");
            userService.validateToken(request);
            List<Document> extractionDocuments = extractionService.getExtractionDocuments(username, projectName);
            List<Extraction> extractions = extractionService.getExtractions(username, projectName);
            List<DocumentGetDTO> documentGetDTOS = new ArrayList<>();
            List<ExtractionGetDTO> extractionGetDTOS = new ArrayList<>();
            for (Document document : extractionDocuments) {
                documentGetDTOS.add(DTOMapper.INSTANCE.convertEntityToDocumentGetDTO(document));
            }
            for (Extraction extraction : extractions) {
                extractionGetDTOS.add(DTOMapper.INSTANCE.convertEntityToExtractionGetDTO(extraction));
            }
            //DocumentAndExtractionDTO documentAndExtractionDTO =
            DocumentAndExtractionDTO documentAndExtractionDTO = new DocumentAndExtractionDTO();
            documentAndExtractionDTO.setExtractions(extractionGetDTOS);
            documentAndExtractionDTO.setDocuments(documentGetDTOS);
            return ResponseEntity.ok(documentAndExtractionDTO);
        }catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("Extraction not found")) {
                System.err.println("Specific error: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(null); // Or use a custom DTO to provide a meaningful response
            }

            // Handle all other exceptions generically
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null); // Or use a custom error DTO
        }
    }
    @GetMapping("/projects/{username}/{projectName}/{extractionName}/documentsAndReport")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<DocumentAndReportDTO> getDocumentsAndReport(
            @PathVariable String username,
            @PathVariable String projectName,
            @PathVariable String extractionName,
            HttpServletRequest request) throws IOException {
        System.out.println("documents and report");
        userService.validateToken(request);
        Extraction extraction = extractionService.getExtraction(username, projectName, extractionName);
        DocumentAndReportDTO documentAndReportDTO = new DocumentAndReportDTO();
        documentAndReportDTO.setAnls(extraction.getAnls());
        documentAndReportDTO.setF1(extraction.getF1());
        documentAndReportDTO.setDocumentNames(extractionService.extractExtractionDocuments(extraction.getExtractions()));


        return ResponseEntity.ok(documentAndReportDTO);
    }

    @PostMapping("/projects/{username}/{projectName}/extractions")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<String> postExtractions(
            @PathVariable String projectName,
            @PathVariable String username,
            @RequestBody ExtractionPostDTO extractionPostDTO,
            HttpServletRequest request) throws IOException {
        System.out.println("extractions");
        userService.validateToken(request);

        Extraction extraction;
        extraction = DTOMapper.INSTANCE.convertExtractionPostDTOToEntity(extractionPostDTO);
        extraction.setOwner(username);
        extraction.setProjectName(projectName);
        List<String> documentNames = extractionPostDTO.getDocumentNames();
        List<SingleExtraction> singleExtractions = new ArrayList<>();
        for (String documentName : documentNames) {
            SingleExtraction singleExtraction = new SingleExtraction();
            singleExtraction.setExtractionName(documentName);
            singleExtractions.add(singleExtraction);
        }
        extraction.setExtractions(singleExtractions);
        extractionService.addExtraction(extraction);

        return ResponseEntity.ok("Extraction started successfully");
    }
    //prompt genereation
    //making prompt instance
    //Waiting for LLM
    //evtl. reporting the current state

    @GetMapping("/projects/{username}/{projectName}/{documentName}/oneExtraction")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<String> getOneExtraction(
            @PathVariable String documentName,
            @PathVariable String projectName,
            @PathVariable String username,
            HttpServletRequest request) throws IOException {
        System.out.println("one extraction");
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
