package com.example.server.controller;

import com.example.server.entity.Label;
import com.example.server.entity.LabelFamily;
import com.example.server.entity.Project;
import com.example.server.rest.dto.*;
import com.example.server.rest.mapper.DTOMapper;
import com.example.server.service.DocumentService;
import com.example.server.service.LabelService;
import com.example.server.service.ProjectService;
import com.example.server.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProjectControllerTest {
    @Mock
    DTOMapper dtoMapper;


    @Mock
    private ProjectService projectService;

    @Mock
    private DocumentService documentService;

    @Mock
    private UserService userService;

    @Mock
    private LabelService labelService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private ProjectController projectController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateUser() {
        // Arrange
        ProjectPostDTO projectPostDTO = new ProjectPostDTO();
        projectPostDTO.setProjectName("TestProject");


        Project project = new Project();
        project.setProjectName("TestProject");
        project.setOwner("TestOwner");

        when(projectService.createProject(any(Project.class), eq("testUser"))).thenReturn(project);

        ProjectGetDTO result = projectController.createUser(projectPostDTO, "testUser", request);

        assertNotNull(result);
        assertEquals("TestProject", result.getProjectName());
        verify(userService).validateToken(request);
        verify(projectService).createProject(any(Project.class), eq("testUser"));
    }

    @Test
    void testGetProjectsByUsername() throws Exception {
        // Arrange
        List<Project> projects = new ArrayList<>();
        Project project = new Project();
        project.setProjectName("TestProject");
        projects.add(project);

        when(projectService.getProjectsByUsername("testUser")).thenReturn(projects);

        // Act
        ResponseEntity<List<ProjectGetDTO>> response = projectController.getProjectsByUsername("testUser", request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        assertEquals("TestProject", response.getBody().get(0).getProjectName());
        verify(userService).validateToken(request);
        verify(projectService).getProjectsByUsername("testUser");
    }
    @Test
    void testPostProjectsByUsername_TriggerIfStatement() {

        String username = "testUser";
        String projectName = "TestProject";

        // Create a ProjectUpdatePostDTO with isToImport = true
        ProjectUpdatePostDTO projectUpdatePostDTO = new ProjectUpdatePostDTO();
        projectUpdatePostDTO.setToImport(true);
        projectUpdatePostDTO.setProjectName("ProjectToImport");

        List<ProjectUpdatePostDTO> projectUpdatePostDTOList = new ArrayList<>();

        Project projectToImport = new Project();
        projectToImport.setOwner(username);
        projectToImport.setToImport(true);
        projectToImport.setProjectName("ProjectToImport");
        projectUpdatePostDTOList.add(projectUpdatePostDTO);

        Project existingProject = new Project();
        existingProject.setOwner(username);
        existingProject.setProjectName(projectName);

        when(userService.validateToken(request)).thenReturn(true);

        when(dtoMapper.convertProjectUpdatePostDTOToEntity(projectUpdatePostDTO))
                .thenReturn(projectToImport);

        doNothing().when(projectService).postProjects(projectToImport, existingProject);


        ResponseEntity<List<LabelFamilyGetDTO>> response = projectController.postProjectsByUsername(
                projectUpdatePostDTOList, username, projectName, request);


        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(userService).validateToken(request);
        verify(projectService, times(1)).postProjects(any(Project.class), any(Project.class));    }

    @Test
    void testPostProjectsByUsername() {
        // Arrange
        List<ProjectUpdatePostDTO> projectUpdatePostDTOList = new ArrayList<>();
        ProjectUpdatePostDTO projectUpdate = new ProjectUpdatePostDTO();
        projectUpdate.setToImport(false);
        projectUpdate.setProjectName("ProjectA");

        LabelFamilyUpdatePostDTO labelFamilyUpdate = new LabelFamilyUpdatePostDTO();
        labelFamilyUpdate.setLabelFamilyName("LabelFamilyA");
        projectUpdate.setLabelFamilies(List.of(labelFamilyUpdate));

        projectUpdatePostDTOList.add(projectUpdate);

        List<LabelFamily> labelFamilies = new ArrayList<>();
        LabelFamily labelFamily = new LabelFamily();
        labelFamily.setLabelFamilyName("LabelFamilyA");
        labelFamilies.add(labelFamily);

        when(labelService.getLabelFamilies(any(LabelFamily.class))).thenReturn(labelFamilies);
        when(labelService.getLabels(any(LabelFamily.class))).thenReturn(new ArrayList<>());

        // Act
        ResponseEntity<List<LabelFamilyGetDTO>> response = projectController.postProjectsByUsername(
                projectUpdatePostDTOList, "testUser", "ProjectA", request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        assertEquals("LabelFamilyA", response.getBody().get(0).getLabelFamilyName());
        verify(userService).validateToken(request);
        verify(labelService).getLabelFamilies(any(LabelFamily.class));
        verify(labelService).getLabels(any(LabelFamily.class));
    }

    @Test
    void testPostProjectsByUsername_LabelLoopTriggered() {
        String username = "testUser";
        String projectName = "TestProject";

        // Mock request data
        ProjectUpdatePostDTO projectUpdatePostDTO = new ProjectUpdatePostDTO();
        projectUpdatePostDTO.setToImport(false);
        projectUpdatePostDTO.setProjectName(projectName);

        LabelFamilyUpdatePostDTO labelFamilyUpdatePostDTO = new LabelFamilyUpdatePostDTO();
        labelFamilyUpdatePostDTO.setLabelFamilyName("TestLabelFamily");
        projectUpdatePostDTO.setLabelFamilies(List.of(labelFamilyUpdatePostDTO));

        List<ProjectUpdatePostDTO> projectUpdatePostDTOList = List.of(projectUpdatePostDTO);

        // Mock database response
        LabelFamily labelFamily = new LabelFamily();
        labelFamily.setOwner(username);
        labelFamily.setProjectName(projectName);

        List <Label> labels = new ArrayList<>();

        Label label1 = new Label();
        label1.setLabelName("Label1");
        labels.add(label1);

        Label label2 = new Label();
        label2.setLabelName("Label2");
        labels.add(label2);
        labelFamily.setLabels(labels);

        LabelGetDTO labelGetDTO1 = new LabelGetDTO();
        labelGetDTO1.setLabelName("Label1");

        LabelGetDTO labelGetDTO2 = new LabelGetDTO();
        labelGetDTO2.setLabelName("Label2");

        LabelFamilyGetDTO labelFamilyGetDTO = new LabelFamilyGetDTO();
        labelFamilyGetDTO.setLabels(new ArrayList<>());

        // Stubbing service and mapper methods
        when(userService.validateToken(request)).thenReturn(true);
        when(labelService.getLabelFamilies(any())).thenReturn(List.of(labelFamily));
        when(labelService.getLabels(labelFamily)).thenReturn(List.of(label1, label2));
        when(dtoMapper.convertEntityToLabelGetDTO(label1)).thenReturn(labelGetDTO1);
        when(dtoMapper.convertEntityToLabelGetDTO(label2)).thenReturn(labelGetDTO2);
        when(dtoMapper.convertEntityToLabelFamilyGetDTO(labelFamily)).thenReturn(labelFamilyGetDTO);

        // Act
        ResponseEntity<List<LabelFamilyGetDTO>> response = projectController.postProjectsByUsername(
                projectUpdatePostDTOList, username, projectName, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        List<LabelFamilyGetDTO> result = response.getBody();
        assertNotNull(result);
        assertEquals(1, result.size());

        LabelFamilyGetDTO returnedLabelFamilyGetDTO = result.get(0);
        assertNotNull(returnedLabelFamilyGetDTO.getLabels());
        assertEquals(2, returnedLabelFamilyGetDTO.getLabels().size());
        assertEquals("Label1", returnedLabelFamilyGetDTO.getLabels().get(0).getLabelName());
        assertEquals("Label2", returnedLabelFamilyGetDTO.getLabels().get(1).getLabelName());

        // Verify methods were called
        verify(labelService, times(1)).getLabels(labelFamily);
    }
}

