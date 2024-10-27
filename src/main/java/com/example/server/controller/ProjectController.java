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
    public List<LabelFamilyGetDTO> postProjectsByUsername(@RequestBody List<ProjectUpdatePostDTO> projectUpdatePostDTOList, @PathVariable String username,@PathVariable String projectName, HttpServletRequest request) {
        userService.validateToken(request);
        System.out.println("Arrived in /projects/{username}/{projectName}");
        List <LabelFamilyUpdatePostDTO> labelFamilyUpdatePostDTOS = new ArrayList<>();
        List <LabelFamily> labelFamiliesRequest = new ArrayList<>();
        // parse representation
        // Iterate over each ProjectUpdatePostDTO in the list
        for (ProjectUpdatePostDTO projectUpdatePostDTOLoop : projectUpdatePostDTOList) {
            // Assuming there's a method `getLabelFamilies` to retrieve the list of LabelFamilyUpdatePostDTO
            System.out.println("outerLoop");
            List<LabelFamilyUpdatePostDTO> labelFamilyUpdatePostDTOs = projectUpdatePostDTOLoop.getLabelFamilies();

            for (LabelFamilyUpdatePostDTO labelFamilyUpdatePostDTOLoop : labelFamilyUpdatePostDTOs) {
                LabelFamily labelFamily = DTOMapper.INSTANCE.convertLabelFamilyUpdatePostDTOToEntity(labelFamilyUpdatePostDTOLoop);
                labelFamily.setOwner(username);
                labelFamiliesRequest.add(labelFamily);
                System.out.println("added" + labelFamily.getLabelFamilyName());
            }
        }

        // fetch from database
        LabelFamily labelFamily = new LabelFamily();
        labelFamily.setOwner(username);
        labelFamily.setProjectName(projectName);
        System.out.println("before call");
        labelService.postLabelFamilies(labelFamiliesRequest, labelFamily);
        System.out.println("after call");
        List<LabelFamily> labelFamiliesDatabase = labelService.getLabelFamilies(labelFamily);
        List<LabelFamilyGetDTO> labelFamiliesGetDTO = new ArrayList<>();


        for (LabelFamily labelFamilyLoop : labelFamiliesDatabase) {
            // Use the mapLabelFamily function to map the labelFamily to labelFamilyGetDTO
            LabelFamilyGetDTO labelFamilyGetDTO = DTOMapper.INSTANCE.convertEntityToLabelFamilyGetDTO(labelFamilyLoop);

            // Transform the labels[] in the labelFamily to labelGetDTOs
            List<LabelGetDTO> labelGetDTOs = new ArrayList<>();
            for (Label label : labelFamilyLoop.getLabels()) {  // Use labelFamilyLoop here
                LabelGetDTO labelGetDTO = DTOMapper.INSTANCE.convertEntityToLabelGetDTO(label); // Map each label to labelGetDTO
                labelGetDTOs.add(labelGetDTO);
            }

            // Set the transformed labels in the labelFamilyGetDTO
            labelFamilyGetDTO.setLabels(labelGetDTOs);

            // Add the transformed labelFamilyGetDTO to the result list
            labelFamiliesGetDTO.add(labelFamilyGetDTO);
        }

        System.out.println("Returns something in /projects/{username}/{projectName}");
        return labelFamiliesGetDTO;
    }


}
