package com.example.server.controller;

import com.example.server.entity.Project;
import com.example.server.entity.User;
import com.example.server.rest.dto.ProjectGetDTO;
import com.example.server.rest.dto.ProjectPostDTO;
import com.example.server.rest.mapper.DTOMapper;
import com.example.server.service.ProjectService;
import com.example.server.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ProjectController {
    private final ProjectService projectService;
    private final UserService userService;

    public ProjectController(ProjectService projectService, UserService userService) {
        this.projectService = projectService;
        this.userService = userService;
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
        // Call projectService to get the list of projects for the given username
        List<Project> projects = projectService.getProjectsByUsername(username);

        // Convert the list of Project entities to a list of ProjectDTOs (using a DTOMapper or similar)
        List<ProjectGetDTO> projectDTOs = projects.stream()
                .map(DTOMapper.INSTANCE::convertEntityProjectGetDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(projectDTOs);
    }

}
