package com.example.server.controller;

import com.example.server.entity.Label;
import com.example.server.entity.LabelFamily;
import com.example.server.rest.dto.*;
import com.example.server.rest.mapper.DTOMapper;
import com.example.server.service.LabelService;
import com.example.server.service.UserService;
import com.google.api.Http;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class LabelControllerTest {
    @Mock
    DTOMapper dtoMapper;
    @Mock
    private UserService userService;

    @Mock
    private LabelService labelService;

    @InjectMocks
    private LabelController labelController;

    @Mock
    private HttpServletRequest httpServletRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testUploadLabelFamilies() throws IOException {
        LabelFamilyPostDTO labelFamilyPostDTO = new LabelFamilyPostDTO();
        labelFamilyPostDTO.setLabelFamilyName("Test Family");

        LabelFamily labelFamily = new LabelFamily();
        labelFamily.setLabelFamilyName("Test Family");
        when(dtoMapper.convertLabelFamilyPostDTOToEntity(any(LabelFamilyPostDTO.class))).thenReturn(labelFamily);

        ResponseEntity<String> response = labelController.uploadLabelFamilies("testProject", "testUser", httpServletRequest, labelFamilyPostDTO);

        verify(userService).validateToken(httpServletRequest);
        verify(labelService).updateLabelFamily(any(LabelFamily.class));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("File uploaded successfully", response.getBody());
    }

    @Test
    void testGetLabelFamilies() throws IOException {
        List <Label> labels = new ArrayList<>();
        Label label = new Label();
        label.setLabelName("Test Label");
        labels.add(label);

        List <LabelFamily> labelFamilies = new ArrayList<>();
        LabelFamily labelFamily = new LabelFamily();
        labelFamily.setOwner("testUser");
        labelFamily.setProjectName("testProject");
        labelFamily.setLabels(labels);
        labelFamilies.add(labelFamily);

        List <LabelGetDTO> labelGetDTOS = new ArrayList<>();
        LabelGetDTO labelGetDTO = new LabelGetDTO();
        labelGetDTO.setLabelName("Test Label");
        labelGetDTOS.add(labelGetDTO);

        LabelFamilyGetDTO labelFamilyGetDTO = new LabelFamilyGetDTO();
        labelFamilyGetDTO.setLabelFamilyName("Test Family");
        labelFamilyGetDTO.setLabels(labelGetDTOS);

        when(labelService.getLabelFamilies(any(LabelFamily.class))).thenReturn(labelFamilies);

        when(dtoMapper.convertEntityToLabelFamilyGetDTO(any(LabelFamily.class))).thenReturn(labelFamilyGetDTO);

        List<LabelFamilyGetDTO> result = labelController.getLabelFamilies("testProject", "testUser", httpServletRequest);

        verify(userService).validateToken(httpServletRequest);
        assertEquals(1, result.size());
    }

    @Test
    void testUploadLabel() throws IOException {
        LabelPostDTO labelPostDTO = new LabelPostDTO();
        labelPostDTO.setLabelName("Test Label");

        LabelFamily labelFamily = new LabelFamily();


        Label label = new Label();
        label.setLabelFamily(labelFamily);
        label.setLabelName("Test Label");
        when(dtoMapper.convertLabelPostDTOToEntity(any(LabelPostDTO.class))).thenReturn(label);

        ResponseEntity<String> response = labelController.uploadLabel("testProject", "testUser", httpServletRequest, labelPostDTO);

        verify(userService).validateToken(httpServletRequest);
        verify(labelService).updateLabel(any(Label.class));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("File uploaded successfully", response.getBody());
    }

    @Test
    void testGetLabels() throws IOException {
        List <Label> labels = new ArrayList<>();

        Label label = new Label();
        label.setLabelName("Test Label");
        labels.add(label);

        LabelNameGetDTO labelNameGetDTO = new LabelNameGetDTO();
        labelNameGetDTO.setLabelName("Test Label");
        LabelFamily labelFamily = new LabelFamily();
        labelFamily.setOwner("testUser");
        labelFamily.setProjectName("testProject");
        labelFamily.setLabelFamilyName("testFamily");

        when(labelService.getLabels(labelFamily)).thenReturn(labels);
        when(dtoMapper.convertEntityToLabelGetDTOTo(any(Label.class))).thenReturn(labelNameGetDTO);

        ResponseEntity<List<LabelNameGetDTO>> response = labelController.getLabels("testProject", "testUser", "testFamily", httpServletRequest);

        verify(userService).validateToken(httpServletRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
