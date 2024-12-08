package com.example.server.controller;

import com.example.server.entity.Document;
import com.example.server.rest.dto.*;
import com.example.server.rest.mapper.DTOMapper;
import com.example.server.service.DocumentService;
import com.example.server.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class DocumentControllerTest {

    @Mock
    DTOMapper dtoMapper;
    @InjectMocks
    private DocumentController documentController;

    @Mock
    private DocumentService documentService;

    @Mock
    private UserService userService;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testUploadFile() throws IOException {
        MockMultipartFile mockFile = new MockMultipartFile("files", "test.pdf", "application/pdf", "Test content".getBytes());

        // Mock a CompletableFuture<Void>
        CompletableFuture<Void> mockFuture = CompletableFuture.completedFuture(null);
        CompletableFuture<Void> mockStartOCRFuture = CompletableFuture.completedFuture(null);

        when(userService.validateToken(request)).thenReturn(true);
        when(documentService.safeInDB(any(Document.class))).thenReturn(mockFuture);
        when(documentService.startOCRProcessAsync(any(Document.class))).thenReturn(mockStartOCRFuture);

        ResponseEntity<String> response = documentController.uploadFile(new MockMultipartFile[]{mockFile}, "testProject", "testUser", request);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("File uploaded successfully", response.getBody());

        verify(userService, times(1)).validateToken(request);
        verify(documentService, times(1)).safeInDB(any(Document.class));
        verify(documentService, times(1)).startOCRProcessAsync(any(Document.class));
    }
    @Test
    void testUploadFile_ThrowEmpty() throws IOException {
        MockMultipartFile mockFile = new MockMultipartFile("files", "test.pdf", "application/pdf", new byte[0]);

        // Mock a CompletableFuture<Void>
        CompletableFuture<Void> mockFuture = CompletableFuture.completedFuture(null);
        CompletableFuture<Void> mockStartOCRFuture = CompletableFuture.completedFuture(null);

        when(userService.validateToken(request)).thenReturn(true);
        when(documentService.safeInDB(any(Document.class))).thenReturn(mockFuture);
        when(documentService.startOCRProcessAsync(any(Document.class))).thenReturn(mockStartOCRFuture);


        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> documentController.uploadFile(new MockMultipartFile[]{mockFile}, "testProject", "testUser", request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("One of the files is empty", exception.getReason());

        verify(userService, times(1)).validateToken(request);

    }

    @Test
    void testUploadFileExtraction() throws IOException {
        // Mock MultipartFile with valid content
        MockMultipartFile mockFile = new MockMultipartFile("files", "test.pdf", "application/pdf", "Test content".getBytes());

        // Mock Document and CompletableFutures
        Document mockDocument = new Document();
        CompletableFuture<Void> mockFuture = CompletableFuture.completedFuture(null);

        // Mock DTOMapper behavior
        DocumentPostDTO mockDocumentPostDTO = new DocumentPostDTO();
        mockDocumentPostDTO.setDocumentName("test.pdf");
        mockDocumentPostDTO.setOwner("testUser");
        mockDocumentPostDTO.setProjectName("testProject");

        when(userService.validateToken(request)).thenReturn(true);
        when(dtoMapper.convertDocumentPostDTOToEntity(any(DocumentPostDTO.class))).thenReturn(mockDocument);

        when(documentService.safeInDB(any(Document.class))).thenReturn(mockFuture);
        when(documentService.startOCRProcessAsync(any(Document.class))).thenReturn(mockFuture);

        // Act
        ResponseEntity<String> response = documentController.uploadFileExtraction(new MockMultipartFile[]{mockFile}, "testProject", "testUser", request);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("File uploaded successfully", response.getBody());

        // Verify service interactions
        verify(documentService, times(1)).safeInDB(any(Document.class));
        verify(documentService, times(1)).startOCRProcessAsync(any(Document.class));
    }


    @Test
    void testUploadFileExtraction_ThrowEmpty() throws IOException {
        MockMultipartFile mockFile = new MockMultipartFile("files", "test.pdf", "application/pdf", new byte[0]);

        // Mock a CompletableFuture<Void>
        CompletableFuture<Void> mockFuture = CompletableFuture.completedFuture(null);
        CompletableFuture<Void> mockStartOCRFuture = CompletableFuture.completedFuture(null);

        when(userService.validateToken(request)).thenReturn(true);
        when(documentService.safeInDB(any(Document.class))).thenReturn(mockFuture);
        when(documentService.startOCRProcessAsync(any(Document.class))).thenReturn(mockStartOCRFuture);


        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> documentController.uploadFileExtraction(new MockMultipartFile[]{mockFile}, "testProject", "testUser", request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("One of the files is empty", exception.getReason());

        verify(userService, times(1)).validateToken(request);

    }


    @Test
    void testDeleteFile() throws IOException {
        when(userService.validateToken(request)).thenReturn(true);
        doNothing().when(documentService).deleteDocument(any(Document.class));

        ResponseEntity<String> response = documentController.deleteFile("\"testFile.pdf\"", "testProject", "testUser", request);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("File uploaded successfully", response.getBody());

        verify(userService, times(1)).validateToken(request);
        verify(documentService, times(1)).deleteDocument(any(Document.class));
    }

    @Test
    void testGetFile() throws IOException {
        List<Document> mockDocuments = new ArrayList<>();
        Document doc = new Document();
        doc.setDocumentName("testDoc");
        mockDocuments.add(doc);

        when(userService.validateToken(request)).thenReturn(true);
        when(documentService.getDocuments("testUser", "testProject")).thenReturn(mockDocuments);

        ResponseEntity<List<DocumentGetDTO>> response = documentController.getFile("testProject", "testUser", request);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("testDoc", response.getBody().get(0).getName());

        verify(userService, times(1)).validateToken(request);
        verify(documentService, times(1)).getDocuments("testUser", "testProject");
    }

    @Test
    void testGetFileToAnnotate() throws IOException {
        Document mockDocument = new Document();
        mockDocument.setDocumentName("testDoc");
        mockDocument.setPdfData("Test PDF Data".getBytes());

        DocumentGetCompleteDTO mockDTO = new DocumentGetCompleteDTO();
        mockDTO.setName("testDoc");


        when(userService.validateToken(request)).thenReturn(true);
        when(documentService.getAnnotationDocument("testUser", "testProject", "testDoc")).thenReturn(mockDocument);
        when(documentService.convertEntityToDocumentGetCompleteDTO(mockDocument)).thenReturn(mockDTO);
        ResponseEntity<DocumentGetCompleteDTO> response = documentController.getFileToAnnotate("testDoc", "testProject", "testUser", request);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("testDoc", response.getBody().getName());
        assertEquals(Base64.getEncoder().encodeToString("Test PDF Data".getBytes()), response.getBody().getBase64PdfData());

        verify(userService, times(1)).validateToken(request);
        verify(documentService, times(1)).getAnnotationDocument("testUser", "testProject", "testDoc");
    }


    @Test
    void testSetAnnotationsToFile() throws IOException {
        DocumentSetCompleteDTO mockDTO = new DocumentSetCompleteDTO();

        when(userService.validateToken(request)).thenReturn(true);
        doNothing().when(documentService).convertDocumentSetCompleteDTOToEntity(eq(mockDTO), eq("testDoc"), eq("testProject"), eq("testUser"));

        ResponseEntity<String> response = documentController.setAnnotationsToFile("testDoc", "testProject", "testUser", mockDTO, request);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Annotations saved successfully!", response.getBody());

        verify(userService, times(1)).validateToken(request);
        verify(documentService, times(1)).convertDocumentSetCompleteDTOToEntity(eq(mockDTO), eq("testDoc"), eq("testProject"), eq("testUser"));
    }


}
