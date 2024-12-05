package com.example.server.controller;

import com.example.server.entity.Document;
import com.example.server.rest.dto.*;
import com.example.server.rest.mapper.DTOMapper;
import com.example.server.service.DocumentService;
import com.example.server.service.ProjectService;
import com.example.server.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@RestController
public class DocumentController {
    private final DocumentService documentService;
    private final UserService userService;

    public DocumentController(ProjectService projectService, DocumentService documentService, UserService userService) {

        this.documentService = documentService;
        this.userService = userService;
    }
    @PostMapping("/projects/{username}/{projectName}/upload") //to make it unique the projectName is a concatenation of username and projectName they are seperated by &
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<String> uploadFile(
            @RequestParam("files") MultipartFile[] files,
            @PathVariable String projectName,
            @PathVariable String username,
            HttpServletRequest request) throws IOException {
        userService.validateToken(request);
        DocumentPostDTO documentPostDTO = new DocumentPostDTO();
        documentPostDTO.setOwner(username);
        documentPostDTO.setProjectName(projectName);

        for (MultipartFile file : files){
            System.out.println("file Empty: " + file.isEmpty());
            if (file.isEmpty()){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One of the files is empty");
            }
            documentPostDTO.setDocumentName(file.getOriginalFilename());
            Document document = DTOMapper.INSTANCE.convertDocumentPostDTOToEntity(documentPostDTO);
            document.setPdfData(file.getBytes());

            documentService.safeInDB(document);
            //for OCR uncomment the follwoing line:
            documentService.startOCRProcessAsync(document);

        }

        return ResponseEntity.ok("File uploaded successfully");
    }

    @DeleteMapping("/projects/{username}/{projectName}/delete") //to make it unique the projectName is a concatenation of username and projectName they are seperated by &
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<String> deleteFile(
            @RequestBody String fileName,
            @PathVariable String projectName,
            @PathVariable String username,
            HttpServletRequest request) throws IOException {
            userService.validateToken(request);

            DocumentDeleteDTO documentDeleteDTO = new DocumentDeleteDTO();
            documentDeleteDTO.setOwner(username);
            documentDeleteDTO.setProjectName(projectName);
            fileName = fileName.replaceAll("^\"|\"$", "");
            documentDeleteDTO.setDocumentName(fileName);

            Document document = DTOMapper.INSTANCE.convertDocumentDeleteDTOToEntity(documentDeleteDTO);
            documentService.deleteDocument(document);



        return ResponseEntity.ok("File uploaded successfully");
    }

    @GetMapping("/projects/{username}/{projectName}/documents") //to make it unique the projectName is a concatenation of username and projectName they are seperated by &
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<List<DocumentGetDTO>> getFile(
            @PathVariable String projectName,
            @PathVariable String username,
            HttpServletRequest request) throws IOException {

        userService.validateToken(request);

        List <Document> documents = documentService.getDocuments(username, projectName);
        List <DocumentGetDTO> documentGetDTOS = new ArrayList<>();
        for (Document document : documents){
            DocumentGetDTO documentGetDTO = DTOMapper.INSTANCE.convertEntityToDocumentGetDTO(document);
            documentGetDTOS.add(documentGetDTO);
        }


        return ResponseEntity.ok(documentGetDTOS);
    }
    @PostMapping("/projects/{username}/{projectName}/uploadExtract")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<String> uploadFileExtraction(
            @RequestParam("files") MultipartFile[] files,
            @PathVariable String projectName,
            @PathVariable String username,
            HttpServletRequest request) throws IOException {
        userService.validateToken(request);


        DocumentPostDTO documentPostDTO = new DocumentPostDTO();
        documentPostDTO.setOwner(username);
        documentPostDTO.setProjectName(projectName);
        for (MultipartFile file : files){
            if (file.isEmpty()){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One of the files is empty");
            }
            documentPostDTO.setDocumentName(file.getOriginalFilename());
            Document document = DTOMapper.INSTANCE.convertDocumentPostDTOToEntity(documentPostDTO);
            document.setPdfData(file.getBytes());

            documentService.safeInDB(document);
            //for OCR uncomment the follwoing line:
            documentService.startOCRProcessAsync(document);

        }

        return ResponseEntity.ok("File uploaded successfully");
    }

    //make an extraction Instance with all the instructionNames
    //Start the prompting process

    @GetMapping("/projects/{username}/{projectName}/{documentName}/annotate")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<DocumentGetCompleteDTO> getFileToAnnotate(
            @PathVariable String documentName,
            @PathVariable String projectName,
            @PathVariable String username,
            HttpServletRequest request) throws IOException {

        userService.validateToken(request);
        Document document = documentService.getAnnotationDocuments(username, projectName, documentName);

        DocumentGetCompleteDTO documentGetCompleteDTO = documentService.convertEntityToDocumentGetCompleteDTO(document);
        documentGetCompleteDTO.setBase64PdfData(Base64.getEncoder().encodeToString(document.getPdfData()));
        //documentGetCompleteDTO.setBoxes(document.getOcrBoxes());
        return ResponseEntity.ok(documentGetCompleteDTO);
    }

    @PostMapping("/projects/{username}/{projectName}/{documentName}/annotations")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<String> setAnnotationsToFile(
            @PathVariable String documentName,
            @PathVariable String projectName,
            @PathVariable String username,
            @RequestBody DocumentSetCompleteDTO documentSetCompleteDTO,
            HttpServletRequest request) throws IOException {

        userService.validateToken(request);
        System.out.println(documentSetCompleteDTO.toString());
        documentService.convertDocumentSetCompleteDTOToEntity(documentSetCompleteDTO,  documentName,  projectName,  username);

        return ResponseEntity.ok("Annotations saved successfully!");
    }
    @PostMapping("/projects/{username}/{projectName}/{documentName}/setAnnotate")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<String> setAsInstruction(
            @PathVariable String documentName,
            @PathVariable String projectName,
            @PathVariable String username,
            HttpServletRequest request) throws IOException {
        userService.validateToken(request);
        documentService.setAsInstruction(documentName, projectName, username);
        return ResponseEntity.ok("Annotations saved successfully!");
    }
    @PostMapping("/projects/{username}/{projectName}/startExtraction")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<String> setAnnotationsToFile(
            @PathVariable String documentName,
            @PathVariable String projectName,
            @RequestBody ExtractionGetDTO extractionDTO,
            HttpServletRequest request) throws IOException {

        userService.validateToken(request);
        //System.out.println(documentSetCompleteDTO.toString());
        //documentService.convertDocumentSetCompleteDTOToEntity(documentSetCompleteDTO,  documentName,  projectName,  username);

        return ResponseEntity.ok("Annotations saved successfully!");
    }


}
