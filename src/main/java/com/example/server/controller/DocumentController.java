package com.example.server.controller;

import com.example.server.entity.Document;
import com.example.server.rest.dto.DocumentDeleteDTO;
import com.example.server.rest.dto.DocumentGetCompleteDTO;
import com.example.server.rest.dto.DocumentGetDTO;
import com.example.server.rest.dto.DocumentPostDTO;
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

import javax.print.Doc;
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
    @PostMapping("/projects/{projectName}/upload") //to make it unique the projectName is a concatenation of username and projectName they are seperated by &
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<String> uploadFile(
            @RequestParam("files") MultipartFile[] files,
            @PathVariable String projectName,
            HttpServletRequest request) throws IOException {
        userService.validateToken(request);
        //extract the owner, username
        String[] parts = projectName.split("&");
        String username = parts[0];
        String actualProjectName = parts[1];
        DocumentPostDTO documentPostDTO = new DocumentPostDTO();
        documentPostDTO.setOwner(username);
        documentPostDTO.setProjectName(actualProjectName);
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

    @GetMapping("/projects/{username}/{projectName}/{documentName}/annotate") //to make it unique the projectName is a concatenation of username and projectName they are seperated by &
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<DocumentGetCompleteDTO> getFileToAnnotate(
            @PathVariable String documentName,
            @PathVariable String projectName,
            @PathVariable String username,
            HttpServletRequest request) throws IOException {

        userService.validateToken(request);
        Document document = documentService.getAnnotationDocuments(username, projectName, documentName);
        DocumentGetCompleteDTO documentGetCompleteDTO = DTOMapper.INSTANCE.convertEntityToDocumentGetCompleteDTO(document);
        documentGetCompleteDTO.setBase64PdfData(Base64.getEncoder().encodeToString(document.getPdfData()));
        return ResponseEntity.ok(documentGetCompleteDTO);
    }
}
