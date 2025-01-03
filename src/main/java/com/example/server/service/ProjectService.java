package com.example.server.service;

import com.example.server.entity.Extraction;
import com.example.server.entity.Label;
import com.example.server.entity.LabelFamily;
import com.example.server.entity.Project;
import com.example.server.repository.ExtractionRepository;
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


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProjectService {
    private final Logger log = LoggerFactory.getLogger(UserService.class);
    private final ProjectRepository projectRepository;
    private final LabelFamilyRepository labelFamilyRepository;
    private final LabelRepository labelRepository;
    private final ExtractionRepository extractionRepository;
    private final ExtractionService extractionService;

    @Autowired
    public ProjectService(ProjectRepository projectRepository, LabelFamilyRepository labelFamilyRepository, LabelRepository labelRepository, ExtractionRepository extractionRepository, ExtractionService extractionService) {
        this.projectRepository = projectRepository;
        this.labelFamilyRepository = labelFamilyRepository;
        this.labelRepository = labelRepository;
        this.extractionRepository = extractionRepository;
        this.extractionService = extractionService;
    }
    public Project createProject(Project project, String owner){
        checkIfProjectExists(project.getProjectName(),owner);
        project.setOwner(owner);
        project = projectRepository.save(project);
        projectRepository.flush();
        log.debug("Created Project:{}",project);
        return project;
    }
    public void postProjects(Project projectToImport, Project projectToUpdate){


        Project projectFromRepositoryToImport = projectRepository.findByProjectNameAndOwner(projectToImport.getProjectName(),projectToImport.getOwner());
        Project projectFromRepositoryToImportCopy = deepCopyProject(projectFromRepositoryToImport);

        List<LabelFamily> labelFamiliesToImport;
        labelFamiliesToImport = labelFamilyRepository.findAllByProjectNameAndOwner(projectToImport.getProjectName(), projectToImport.getOwner());

        for (LabelFamily labelFamilyToImport : labelFamiliesToImport){

                LabelFamily labelFamilyToImportCopy = createDeepCopy(labelFamilyToImport);
                labelFamilyToImportCopy.setProjectName(projectToUpdate.getProjectName());
                Optional <LabelFamily> labelFamilyInRepoWithSameName = labelFamilyRepository.findByOwnerAndProjectNameAndLabelFamilyName(projectToImport.getOwner(), projectToUpdate.getProjectName(), labelFamilyToImportCopy.getLabelFamilyName());
                if (labelFamilyInRepoWithSameName.isPresent()){
                    labelFamilyToImportCopy.setLabelFamilyName(projectFromRepositoryToImportCopy.getProjectName()+"_"+ labelFamilyToImportCopy.getLabelFamilyName());

                }else{
                    labelFamilyToImportCopy.setLabelFamilyName(labelFamilyToImportCopy.getLabelFamilyName());

                }
                labelFamilyRepository.save(labelFamilyToImportCopy);
                labelFamilyRepository.flush();

                //if error, move this up, maybe labelFamilyToImportCopy is not available here anymore...
                Optional<LabelFamily> labelFamilyToGetIdFromOpt =labelFamilyRepository.findByOwnerAndProjectNameAndLabelFamilyName(labelFamilyToImportCopy.getOwner(), projectToImport.getProjectName(), labelFamilyToImport.getLabelFamilyName());
                if (labelFamilyToGetIdFromOpt.isPresent()){
                    System.out.println("labelFamilyPresent");
                    LabelFamily labelFamilyToGetIdFrom = labelFamilyToGetIdFromOpt.get();
                    List<Label> labelsToImport;
                    labelsToImport = labelRepository.findAllByLabelFamilyId(labelFamilyToGetIdFrom.getId());

                    for (Label labelToImport : labelsToImport){
                        Label labelToImportCopy = deepCopyLabel(labelToImport, projectToUpdate.getProjectName(), labelFamilyToImportCopy);
                        labelToImportCopy.setFamilyProjectName(projectToUpdate.getProjectName());
                        labelRepository.save(labelToImportCopy);
                        labelRepository.flush();

                    }
                }



        }


    }
    public void checkIfProjectExists(String project, String owner){
        Project projectByRepository = projectRepository.findByProjectNameAndOwner(project, owner);
        if (projectByRepository != null){
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Project with name '" + projectByRepository.getProjectName() + "' already exists.");
        }
    }
    public List<Project> getProjectsByUsername(String username) throws Exception {
        List<Project> projects = projectRepository.findAllByOwner(username);
        for (Project project : projects){
            calculateF1OfProject(username, project.getProjectName());
            }

        List <Project> projectsWithF1 = projectRepository.findAllByOwner(username);
        return projectsWithF1;
    }
    public void calculateF1OfProject(String owner, String projectName) throws Exception {
        System.out.println("Calculating F1 for project: " + projectName);
        Project project = projectRepository.findByProjectNameAndOwner(projectName, owner);
        if (project == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Project with name '" + projectName + "' not found.");
        }
        List<Extraction> extractions = extractionRepository.findAllByOwnerAndProjectName(owner, projectName);
        List<Double> f1 = new ArrayList<>();

        for (Extraction extraction : extractions){
            if (extraction.getF1() == null){
                extractionService.calculateF1ForExtraction(owner, projectName, extraction.getExtractionName());
            }
            f1.add(extraction.getF1());
        }
        Double averageF1 = f1.stream().mapToDouble(val -> val).average().orElse(0.0);
        project.setF1(averageF1);
        projectRepository.save(project);
        projectRepository.flush();
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
        copy.setProjectName(original.getProjectName());

        return copy;
    }




}
