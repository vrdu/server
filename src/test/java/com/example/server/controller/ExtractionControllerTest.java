package com.example.server.controller;

import com.example.server.entity.Document;
import com.example.server.entity.Extraction;
import com.example.server.rest.dto.DocumentAndExtractionDTO;
import com.example.server.rest.dto.ExtractionPostDTO;
import com.example.server.rest.dto.DocumentGetDTO;
import com.example.server.rest.dto.ExtractionGetDTO;
import com.example.server.rest.mapper.DTOMapper;
import com.example.server.service.ExtractionService;
import com.example.server.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExtractionControllerTest {

    @Mock
    private ExtractionService extractionService;

    @Mock
    private UserService userService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private ExtractionController extractionController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetDocumentsAndExtractions() throws IOException {
        // Arrange
        String username = "testUser";
        String projectName = "testProject";

        List<Document> documents = List.of(new Document());
        List<Extraction> extractions = List.of(new Extraction());
        when(extractionService.getExtractionDocuments(username, projectName)).thenReturn(documents);
        when(extractionService.getExtractions(username, projectName)).thenReturn(extractions);

        DocumentGetDTO documentDTO = new DocumentGetDTO();
        ExtractionGetDTO extractionDTO = new ExtractionGetDTO();

        mockStatic(DTOMapper.class);
        when(DTOMapper.INSTANCE.convertEntityToDocumentGetDTO(any(Document.class))).thenReturn(documentDTO);
        when(DTOMapper.INSTANCE.convertEntityToExtractionGetDTO(any(Extraction.class))).thenReturn(extractionDTO);

        // Act
        ResponseEntity<DocumentAndExtractionDTO> response = extractionController.getDocumentsAndExtractions(
                projectName, username, request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().getDocuments().size());
        assertEquals(1, response.getBody().getExtractions().size());

        verify(userService).validateToken(request);
        verify(extractionService).getExtractionDocuments(username, projectName);
        verify(extractionService).getExtractions(username, projectName);
    }

    @Test
    void testPostExtractions() throws IOException {
        // Arrange
        String username = "testUser";
        String projectName = "testProject";

        ExtractionPostDTO extractionPostDTO = new ExtractionPostDTO();
        Extraction extraction = new Extraction();

        mockStatic(DTOMapper.class);
        when(DTOMapper.INSTANCE.convertExtractionPostDTOToEntity(extractionPostDTO)).thenReturn(extraction);
        doNothing().when(extractionService).addExtraction(extraction);

        // Act
        ResponseEntity<String> response = extractionController.postExtractions(
                projectName, username, extractionPostDTO, request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Extraction started successfully", response.getBody());

        verify(userService).validateToken(request);
        verify(extractionService).addExtraction(extraction);
    }

    @Test
    void testGetOneExtraction() throws IOException {
        // Arrange
        String username = "testUser";
        String projectName = "testProject";
        String documentName = "testDocument";

        doNothing().when(userService).validateToken(request);

        // Act
        ResponseEntity<String> response = extractionController.getOneExtraction(
                documentName, projectName, username, request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("s", response.getBody());

        verify(userService).validateToken(request);
    }
}
