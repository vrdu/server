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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ProjectController {
    private final ProjectService projectService;
    private final DocumentService documentService;
    private final UserService userService;
    private final LabelService labelService;

    public ProjectController(ProjectService projectService, DocumentService documentService, UserService userService, LabelService labelService) {
        this.projectService = projectService;
        this.documentService = documentService;
        this.userService = userService;
        this.labelService = labelService;
    }
    @PostMapping("/projects/create/{username}")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ProjectGetDTO createUser(@RequestBody ProjectPostDTO projectPostDTO,@PathVariable String username, HttpServletRequest request){
        userService.validateToken(request);
        Project userInput = DTOMapper.INSTANCE.convertProjectPostDTOToEntity(projectPostDTO);
        Project createdProject = projectService.createProject(userInput, username);
        return DTOMapper.INSTANCE.convertEntityProjectGetDTO(createdProject);
    }
    @GetMapping("/projects/{username}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<List<ProjectGetDTO>> getProjectsByUsername(@PathVariable String username, HttpServletRequest request) {
        userService.validateToken(request);
        List<Project> projects = projectService.getProjectsByUsername(username);

        // Convert the list of Project entities to a list of ProjectDTOs (using a DTOMapper or similar)
        List<ProjectGetDTO> projectDTOs = projects.stream()
                .map(DTOMapper.INSTANCE::convertEntityProjectGetDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(projectDTOs);
    }

    @PostMapping("/projects/{username}/{projectName}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<List<LabelFamilyGetDTO>>  postProjectsByUsername(@RequestBody List<ProjectUpdatePostDTO> projectUpdatePostDTOList, @PathVariable String username,@PathVariable String projectName, HttpServletRequest request) {
        userService.validateToken(request);
        System.out.println("Received projectUpdatePostDTOList: " + projectUpdatePostDTOList);
        Project project2 = new Project();
        project2.setOwner(username);
        project2.setProjectName(projectName);

        LabelFamily labelFamily2 = new LabelFamily();
        labelFamily2.setOwner(username);
        labelFamily2.setProjectName(projectName);
        for (ProjectUpdatePostDTO projectUpdatePostDTOLoop : projectUpdatePostDTOList) {
            if (projectUpdatePostDTOLoop.isToImport()){
                Project project = DTOMapper.INSTANCE.convertProjectUpdatePostDTOToEntity(projectUpdatePostDTOLoop);
                project.setOwner(username);
                projectService.postProjects(project, project2);
            }else{
                for (LabelFamilyUpdatePostDTO labelFamilyUpdatePostDTOLoop : projectUpdatePostDTOLoop.getLabelFamilies()) {
                    LabelFamily labelFamily = DTOMapper.INSTANCE.convertLabelFamilyUpdatePostDTOToEntity(labelFamilyUpdatePostDTOLoop);
                    labelFamily.setProjectName(projectUpdatePostDTOLoop.getProjectName());
                    labelFamily.setOwner(username);
                    labelService.postLabelFamilies(labelFamily, labelFamily2);
                }
            }


        }


        // fetch from database
        LabelFamily labelFamilyFetch = new LabelFamily();
        labelFamilyFetch.setOwner(username);
        labelFamilyFetch.setProjectName(projectName);
        List<LabelFamily> labelFamiliesDatabase = labelService.getLabelFamilies(labelFamilyFetch);

        List<LabelFamilyGetDTO> labelFamiliesGetDTOs = new ArrayList<>();

        System.out.println("labelFamilyDatabese: " +labelFamiliesDatabase);
        for (LabelFamily labelFamilyLoop : labelFamiliesDatabase) {
            System.out.println("labelFamilyLoop: " + labelFamilyLoop);
            // Use the mapLabelFamily function to map the labelFamily to labelFamilyGetDTO
            LabelFamilyGetDTO labelFamilyGetDTO = DTOMapper.INSTANCE.convertEntityToLabelFamilyGetDTO(labelFamilyLoop);

            // Transform the labels[] in the labelFamily to labelGetDTOs
            List<LabelGetDTO> labelGetDTOs = new ArrayList<>();
            List<Label> labelsDatabase = labelService.getLabels(labelFamilyLoop);
            for (Label label : labelsDatabase) {  // Use labelFamilyLoop here
                System.out.println("hi");
                LabelGetDTO labelGetDTO = DTOMapper.INSTANCE.convertEntityToLabelGetDTO(label); // Map each label to labelGetDTO
                labelGetDTOs.add(labelGetDTO);

            }
            // Set the transformed labels in the labelFamilyGetDTO
            labelFamilyGetDTO.setLabels(labelGetDTOs);

            // Add the transformed labelFamilyGetDTO to the result list
            labelFamiliesGetDTOs.add(labelFamilyGetDTO);
        }

        return ResponseEntity.ok(labelFamiliesGetDTOs);
    }


}
