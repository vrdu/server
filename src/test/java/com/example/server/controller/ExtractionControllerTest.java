package com.example.server.controller;

import com.example.server.entity.Document;
import com.example.server.entity.Extraction;
import com.example.server.rest.dto.DocumentAndExtractionDTO;
import com.example.server.rest.dto.DocumentGetDTO;
import com.example.server.rest.dto.ExtractionGetDTO;
import com.example.server.rest.dto.ExtractionPostDTO;
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

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ExtractionControllerTest {

    @Mock
    private ExtractionService extractionService;

    @Mock
    private UserService userService;

    @InjectMocks
    private ExtractionController extractionController;

    @Mock
    private DTOMapper dtoMapper;

    @Mock
    private HttpServletRequest httpServletRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetDocumentsAndExtractions() throws Exception {
        // Arrange
        String username = "testUser";
        String projectName = "testProject";

        Document document = new Document();
        document.setId(1L);
        Extraction extraction = new Extraction();
        extraction.setId(1L);

        List<Document> documents = Arrays.asList(document);
        List<Extraction> extractions = Arrays.asList(extraction);

        DocumentGetDTO documentGetDTO = new DocumentGetDTO();
        ExtractionGetDTO extractionGetDTO = new ExtractionGetDTO();

        when(extractionService.getExtractionDocuments(username, projectName)).thenReturn(documents);
        when(extractionService.getExtractions(username, projectName)).thenReturn(extractions);
        when(dtoMapper.convertEntityToDocumentGetDTO(document)).thenReturn(documentGetDTO);
        when(dtoMapper.convertEntityToExtractionGetDTO(extraction)).thenReturn(extractionGetDTO);

        // Act
        ResponseEntity<DocumentAndExtractionDTO> response = extractionController.getDocumentsAndExtractions(
                projectName, username, httpServletRequest);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        DocumentAndExtractionDTO responseBody = response.getBody();
        assert responseBody != null;
        assertEquals(1, responseBody.getDocuments().size());
        assertEquals(1, responseBody.getExtractions().size());

        verify(userService, times(1)).validateToken(httpServletRequest);
        verify(extractionService, times(1)).getExtractionDocuments(username, projectName);
        verify(extractionService, times(1)).getExtractions(username, projectName);
    }

    @Test
    void testPostExtractions() throws Exception {
        // Arrange
        String username = "testUser";
        String projectName = "testProject";

        ExtractionPostDTO extractionPostDTO = new ExtractionPostDTO();
        Extraction extraction = new Extraction();

        when(dtoMapper.convertExtractionPostDTOToEntity(extractionPostDTO)).thenReturn(extraction);

        // Act
        ResponseEntity<String> response = extractionController.postExtractions(
                projectName, username, extractionPostDTO, httpServletRequest);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Extraction started successfully", response.getBody());

        verify(userService, times(1)).validateToken(httpServletRequest);
        verify(dtoMapper, times(1)).convertExtractionPostDTOToEntity(extractionPostDTO);
        verify(extractionService, times(1)).addExtraction(extraction);
    }
}
