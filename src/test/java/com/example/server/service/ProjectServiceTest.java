package com.example.server.service;

import com.example.server.entity.Label;
import com.example.server.entity.LabelFamily;
import com.example.server.entity.Project;
import com.example.server.repository.LabelFamilyRepository;
import com.example.server.repository.LabelRepository;
import com.example.server.repository.ProjectRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProjectServiceTest {


    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private LabelFamilyRepository labelFamilyRepository;

    @Mock
    private LabelRepository labelRepository;

    @Mock
    private Logger log;

    @InjectMocks
    private ProjectService projectService;

    private Project testProject;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testProject = new Project();
        testProject.setProjectName("testProject");
    }

    @AfterEach
    void tearDown() {
        Mockito.reset(projectRepository, labelFamilyRepository);
    }

    @Test
    void createProject_Success() {

        String owner = "testUser";

        when(projectRepository.findByProjectNameAndOwner("Test Project", owner)).thenReturn(null);
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project savedProject = invocation.getArgument(0);
            savedProject.setId(1L);
            return savedProject;
        });


        Project createdProject = projectService.createProject(testProject, owner);

        // Assert
        assertNotNull(createdProject);
        assertEquals("testProject", createdProject.getProjectName());
        assertEquals(owner, createdProject.getOwner());
        assertEquals(0, createdProject.getAnls());
        assertEquals(0, createdProject.getF1());
        assertNotNull(createdProject.getId());
        verify(projectRepository).save(testProject);
        verify(projectRepository).flush();
    }

    @Test
    void createProject_ThrowsExceptionIfProjectExists() {
        // Arrange
        String owner = "testUser";
        testProject.setOwner(owner);

        when(projectRepository.findByProjectNameAndOwner("testProject", owner)).thenReturn(testProject);

        Project newProject = new Project();
        newProject.setProjectName("testProject");

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                projectService.createProject(newProject, owner)
        );

        assertEquals("409 CONFLICT \"Project with name 'testProject' already exists.\"", exception.getMessage());
        verify(projectRepository, never()).save(any());
        verify(projectRepository, never()).flush();
    }


    @Test
    void postProjects_Success() {
        // Arrange
        Project projectToImport = new Project();
        projectToImport.setProjectName("ImportProject");
        projectToImport.setOwner("user1");

        Project projectToUpdate = new Project();
        projectToUpdate.setProjectName("UpdateProject");
        projectToUpdate.setOwner("user1");

        Project importedProject = new Project();
        importedProject.setProjectName("ImportProject");
        importedProject.setOwner("user1");

        when(projectRepository.findByProjectNameAndOwner("ImportProject", "user1")).thenReturn(importedProject);

        LabelFamily labelFamilyToImport = new LabelFamily();
        labelFamilyToImport.setLabelFamilyName("Family1");
        labelFamilyToImport.setProjectName("ImportProject");
        labelFamilyToImport.setOwner("user1");
        labelFamilyToImport.setLabelFamilyDescription("Test Description");

        List<LabelFamily> labelFamiliesToImport = new ArrayList<>();
        labelFamiliesToImport.add(labelFamilyToImport);

        when(labelFamilyRepository.findAllByProjectNameAndOwner("ImportProject", "user1")).thenReturn(labelFamiliesToImport);
        when(labelFamilyRepository.findByOwnerAndProjectNameAndLabelFamilyName("user1", "UpdateProject", "Family1"))
                .thenReturn(Optional.empty());

        Label labelToImport = new Label();
        labelToImport.setLabelName("Label1");
        labelToImport.setLabelDescription("Label Description");
        labelToImport.setFamilyOwner("user1");
        labelToImport.setFamilyProjectName("ImportProject");
        labelToImport.setFamilyName("Family1");

        List<Label> labelsToImport = new ArrayList<>();
        labelsToImport.add(labelToImport);

        when(labelFamilyRepository.findByOwnerAndProjectNameAndLabelFamilyName("user1", "ImportProject", "Family1"))
                .thenReturn(Optional.of(labelFamilyToImport));

        when(labelRepository.findAllByLabelFamilyId(labelFamilyToImport.getId())).thenReturn(labelsToImport);

        // Act
        projectService.postProjects(projectToImport, projectToUpdate);

        // Assert
        verify(projectRepository).findByProjectNameAndOwner("ImportProject", "user1");
        verify(labelFamilyRepository).findAllByProjectNameAndOwner("ImportProject", "user1");
        verify(labelFamilyRepository).save(any(LabelFamily.class));
        verify(labelRepository).save(any(Label.class));
    }

    @Test
    void postProjects_LabelFamilyExists() {
        // Arrange
        Project projectToImport = new Project();
        projectToImport.setProjectName("ImportProject");
        projectToImport.setOwner("user1");

        Project projectToUpdate = new Project();
        projectToUpdate.setProjectName("UpdateProject");
        projectToUpdate.setOwner("user1");

        LabelFamily existingLabelFamily = new LabelFamily();
        existingLabelFamily.setLabelFamilyName("Family1");
        existingLabelFamily.setProjectName("UpdateProject");
        existingLabelFamily.setOwner("user1");

        when(labelFamilyRepository.findByOwnerAndProjectNameAndLabelFamilyName("user1", "UpdateProject", "Family1"))
                .thenReturn(Optional.of(existingLabelFamily));

        when(projectRepository.findByProjectNameAndOwner("ImportProject", "user1")).thenReturn(projectToImport);



        projectService.postProjects(projectToImport, projectToUpdate);

        // Assert
        verify(labelFamilyRepository, never()).save(existingLabelFamily);
        verify(projectRepository).findByProjectNameAndOwner("ImportProject", "user1");
    }
    @Test
    void postProjects_LabelFamilyNameConflict() {

        String owner = "user1";
        String projectToImportName = "ImportProject";
        String projectToUpdateName = "UpdateProject";

        Project projectToImport = new Project();
        projectToImport.setProjectName(projectToImportName);
        projectToImport.setOwner(owner);

        Project projectToUpdate = new Project();
        projectToUpdate.setProjectName(projectToUpdateName);
        projectToUpdate.setOwner(owner);

        Project importedProject = new Project();
        importedProject.setProjectName(projectToImportName);
        importedProject.setOwner(owner);

        when(projectRepository.findByProjectNameAndOwner(projectToImportName, owner)).thenReturn(importedProject);


        LabelFamily labelFamilyToImport = new LabelFamily();
        labelFamilyToImport.setLabelFamilyName("Family1");
        labelFamilyToImport.setProjectName(projectToImportName);
        labelFamilyToImport.setOwner(owner);

        List<LabelFamily> labelFamiliesToImport = new ArrayList<>();
        labelFamiliesToImport.add(labelFamilyToImport);

        when(labelFamilyRepository.findAllByProjectNameAndOwner(projectToImportName, owner)).thenReturn(labelFamiliesToImport);


        LabelFamily conflictingLabelFamily = new LabelFamily();
        conflictingLabelFamily.setLabelFamilyName("Family1");
        conflictingLabelFamily.setProjectName(projectToUpdateName);
        conflictingLabelFamily.setOwner(owner);

        when(labelFamilyRepository.findByOwnerAndProjectNameAndLabelFamilyName(owner, projectToUpdateName, "Family1"))
                .thenReturn(Optional.of(conflictingLabelFamily));


        projectService.postProjects(projectToImport, projectToUpdate);


        verify(labelFamilyRepository).save(argThat(labelFamily ->
                labelFamily.getLabelFamilyName().equals("ImportProject_Family1")
        ));
    }

    @Test
    void getProjectsByUsername_Success() {

        String username = "testUser";
        List<Project> expectedProjects = new ArrayList<>();

        Project project1 = new Project();
        project1.setProjectName("Project1");
        project1.setOwner(username);
        expectedProjects.add(project1);

        Project project2 = new Project();
        project2.setProjectName("Project2");
        project2.setOwner(username);
        expectedProjects.add(project2);

        when(projectRepository.findAllByOwner(username)).thenReturn(expectedProjects);


        List<Project> actualProjects = projectService.getProjectsByUsername(username);


        assertNotNull(actualProjects);
        assertEquals(2, actualProjects.size());
        assertEquals("Project1", actualProjects.get(0).getProjectName());
        assertEquals("Project2", actualProjects.get(1).getProjectName());
        assertEquals(username, actualProjects.get(0).getOwner());
        assertEquals(username, actualProjects.get(1).getOwner());
        verify(projectRepository).findAllByOwner(username);
    }



}