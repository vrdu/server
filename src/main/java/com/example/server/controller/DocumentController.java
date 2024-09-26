package com.example.server.controller;

import com.example.server.entity.Document;
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

import java.io.IOException;

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
        System.out.println("arrived in backend");
        userService.validateToken(request);

        //extract the owner, username
        String[] parts = projectName.split("&");
        String username = parts[0];
        String actualProjectName = parts[1];
        DocumentPostDTO documentPostDTO = new DocumentPostDTO();
        documentPostDTO.
        documentPostDTO.setProjectName(actualProjectName);
        System.out.println("mapping ok");
        for (MultipartFile file : files){
            if (file.isEmpty()){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One of the files is empty");
            }
            Document document = DTOMapper.INSTANCE.convertDocumentPostDTOToEntity(documentPostDTO);
            document.setPdfData(file.getBytes());
            documentService.safeInDB(document);
            documentService.startOCRProcessAsync(document);
        }

        return ResponseEntity.ok("File uploaded successfully");
    }
}
