package com.example.server.service;

import com.example.server.entity.Label;
import com.example.server.entity.LabelFamily;
import com.example.server.entity.Project;
import com.example.server.repository.LabelFamilyRepository;
import com.example.server.repository.LabelRepository;
import com.example.server.repository.ProjectRepository;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


import java.util.List;

@Service
@Transactional
public class ProjectService {
    private final Logger log = LoggerFactory.getLogger(UserService.class);
    private final ProjectRepository projectRepository;
    private final LabelFamilyRepository labelFamilyRepository;
    private final LabelRepository labelRepository;

    @Autowired
    public ProjectService(ProjectRepository projectRepository, LabelFamilyRepository labelFamilyRepository, LabelRepository labelRepository) {
        this.projectRepository = projectRepository;
        this.labelFamilyRepository = labelFamilyRepository;
        this.labelRepository = labelRepository;
    }
    public Project createProject(Project project, String owner){
        checkIfProjectExists(project.getProjectName(),owner);
        project.setAnls(0);
        project.setF1(0);
        project.setOwner(owner);
        project = projectRepository.save(project);
        projectRepository.flush();
        log.debug("Created Project:{}",project);
        return project;
    }
    public void postProjects(List<Project> projects, Project projectToUpdate){

        for (Project projectToImport : projects){
            Project projectFromRepositoryToImport = projectRepository.findByProjectNameAndOwner(projectToImport.getProjectName(),projectToImport.getOwner());
            Project projectFromRepositoryToImportCopy = deepCopyProject(projectFromRepositoryToImport);
            List<LabelFamily> labelFamiliesToImport;
            labelFamiliesToImport = labelFamilyRepository.findAllByProjectNameAndOwner(projectFromRepositoryToImportCopy.getProjectName(), projectToUpdate.getOwner());

            for (LabelFamily labelFamilyToImport : labelFamiliesToImport){
                    LabelFamily labelFamilyToImportCopy = createDeepCopy(labelFamilyToImport);
                    labelFamilyToImportCopy.setProjectName(projectToUpdate.getProjectName());

                    List<Label> labelsToImport;
                    labelsToImport = labelRepository.findAllByLabelFamilyId(labelFamilyToImportCopy.getId());

                    for (Label labelToImport : labelsToImport){
                        Label labelToImportCopy = deepCopyLabel(labelToImport, projectToUpdate.getProjectName(), labelFamilyToImportCopy);
                        labelToImportCopy.setFamilyProjectName(projectToUpdate.getProjectName());
                        labelRepository.save(labelToImportCopy);
                    }

                    labelFamilyRepository.save(labelFamilyToImportCopy);
            }
            projectFromRepositoryToImportCopy.setProjectName(projectToUpdate.getProjectName());
            projectRepository.save(projectFromRepositoryToImportCopy);
        }
    }
    public void checkIfProjectExists(String project, String owner){
        Project projectByRepository = projectRepository.findByProjectNameAndOwner(project, owner);
        if (projectByRepository != null){
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Project with name '" + projectByRepository.getProjectName() + "' already exists.");
        }
    }
    public List<Project> getProjectsByUsername(String username){
        List<Project> projects = projectRepository.findAllByOwner(username);
        return projects;
    }

    //Helpers
    private Label deepCopyLabel(Label labelToCopy, String projectName, LabelFamily labelFamily){
        Label copy = new Label();
        copy.setLabelName(labelToCopy.getLabelName());
        copy.setLabelDescription(labelToCopy.getLabelDescription());
        copy.setFamilyOwner(labelToCopy.getFamilyOwner());
        copy.setFamilyProjectName(projectName);
        copy.setFamilyName(labelToCopy.getFamilyName());
        copy.setLabelFamily(labelFamily);
        return copy;
    }

    private LabelFamily createDeepCopy(LabelFamily original) {
        LabelFamily copy = new LabelFamily();
        copy.setLabelFamilyName(original.getLabelFamilyName());
        copy.setProjectName(original.getProjectName());
        copy.setOwner(original.getOwner());
        copy.setLabelFamilyDescription(original.getLabelFamilyDescription());
        return copy;
    }
    private Project deepCopyProject(Project original){
        Project copy = new Project();
        copy.setOwner(original.getOwner());

        return copy;
    }




}
