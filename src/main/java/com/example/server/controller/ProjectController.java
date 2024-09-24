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
    public ProjectGetDTO createUser(@RequestBody ProjectPostDTO projectPostDTO , HttpServletRequest request){
        userService.validateToken(request);
        System.out.println(projectPostDTO.getProjectName());
        Project userInput = DTOMapper.INSTANCE.convertProjectPostDTOToEntity(projectPostDTO);
        System.out.println(userInput.getProjectName());
        Project createdProject = projectService.createProject(userInput);
        return DTOMapper.INSTANCE.convertEntityProjectGetDTO(createdProject);
    }
}
