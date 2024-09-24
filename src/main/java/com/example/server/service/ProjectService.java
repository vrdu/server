package com.example.server.service;

import com.example.server.entity.Project;
import com.example.server.entity.User;
import com.example.server.repository.ProjectRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class ProjectService {
    private final Logger log = LoggerFactory.getLogger(UserService.class);
    private final ProjectRepository projectRepository;

    @Autowired
    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }
    public Project createProject(Project project){
        checkIfProjectExists(project.getProjectName());
        project.setAnls(0);
        project.setF1(0);
        project = projectRepository.save(project);
        projectRepository.flush();
        log.debug("Created Project:{}",project);
        return project;
    }

    public void checkIfProjectExists(String project){
        Project projectByRepository = projectRepository.findByprojectName(project);
        if (projectByRepository != null){
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Project with name '" + projectByRepository.getProjectName() + "' already exists.");
        }
    }
}