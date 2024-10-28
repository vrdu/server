package com.example.server.service;

import com.example.server.entity.Label;
import com.example.server.entity.LabelFamily;
import com.example.server.repository.LabelFamilyRepository;
import com.example.server.repository.LabelRepository;
import jakarta.persistence.*;
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
public class LabelService {
    private final Logger log = LoggerFactory.getLogger(UserService.class);
    private final LabelRepository labelRepository;
    private final LabelFamilyRepository labelFamilyRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public LabelService(LabelRepository labelRepository, LabelFamilyRepository labelFamilyRepository) {
        this.labelRepository = labelRepository;
        this.labelFamilyRepository = labelFamilyRepository;
    }

    public void updateLabelFamily(LabelFamily labelFamily) {
        // Loop through the incoming label families
        LabelFamily newLabelFamily = labelFamily;
        if (newLabelFamily.getLabelFamilyName() == null) {
            newLabelFamily.setLabelFamilyName(" ");
        }
        if (newLabelFamily.getLabelFamilyDescription() == null) {
            newLabelFamily.setLabelFamilyDescription(" ");
        }
        String usernameToExtractFromDB = newLabelFamily.getLabelFamilyName();

        if (!(newLabelFamily.getOldLabelFamilyName() == null)) {
            usernameToExtractFromDB = newLabelFamily.getOldLabelFamilyName();
        }
        // Retrieve the existing label family from the repository (using owner, projectName, and id for uniqueness)
        Optional<LabelFamily> existingLabelFamilyOpt = labelFamilyRepository
                .findByOwnerAndProjectNameAndLabelFamilyName(newLabelFamily.getOwner(), newLabelFamily.getProjectName(), usernameToExtractFromDB);

        if (existingLabelFamilyOpt.isPresent()) {
            LabelFamily existingLabelFamily = existingLabelFamilyOpt.get();

            //check, if creation of FamilyWith same name
            if (newLabelFamily.getRegister().equals(true)){
                System.out.println("Tried to make labelFamily with same name");
                throw new ResponseStatusException(HttpStatus.CONFLICT,"LabelFamily with name '" + newLabelFamily.getLabelFamilyName() + "' already exists.");
            }

            checkUniqueUpdatedLabelFamilyName(newLabelFamily);

            if ((existingLabelFamily.getLabelFamilyName().equals(newLabelFamily.getLabelFamilyName())) && newLabelFamily.getRegister() == true) {
                System.out.println("Same labelFamilyName");
                throw new ResponseStatusException(HttpStatus.CONFLICT, "The name of a labelFamily must be unique in a project.");
            }
            // Compare the existing label family with the new one
            boolean familyChanged = checkIfFamilyChanged(existingLabelFamily, newLabelFamily);

            if (familyChanged) {
                System.out.println("familyChangedTrue");

                updateLabelFamily(existingLabelFamily, newLabelFamily);
                labelFamilyRepository.save(existingLabelFamily);
            }
        } else {
            System.out.println("familyDoesNotExist");
            System.out.println(String.format("Id: %s, Index: %s, description: %s, familyName: %s, owner: %s, projectName: %s, oldFamilyName: %s", newLabelFamily.getId(), newLabelFamily.getIndex(), newLabelFamily.getLabelFamilyDescription(), newLabelFamily.getLabelFamilyName(), newLabelFamily.getOwner(), newLabelFamily.getProjectName(), newLabelFamily.getOldLabelFamilyName()));
            // If label family does not exist, add it
            labelFamilyRepository.save(newLabelFamily);  // Save the new label family along with labels (cascade)
        }
    }
        public void postLabelFamilies(List<LabelFamily> familiesToAdd, LabelFamily labelFamilyToAddTo){

            System.out.println("projectName of labelFamilyToAddTo: " + labelFamilyToAddTo.getProjectName());

            for(LabelFamily labelFamily : familiesToAdd){
            System.out.println(String.format("trying to post labelFamily %s", labelFamily.getLabelFamilyName()));
            Optional<LabelFamily> labelFamilyToAddDBOpt = labelFamilyRepository.findByOwnerAndProjectNameAndLabelFamilyName(labelFamily.getOwner(),labelFamily.getProjectName(),labelFamily.getLabelFamilyName());
            if (labelFamilyToAddDBOpt.isPresent()){
                LabelFamily labelFamilyToAddDB = labelFamilyToAddDBOpt.get();
                LabelFamily deepCopyLabelFamily = createDeepCopy(labelFamilyToAddDB);
                System.out.println("deepCopy: Description: " + deepCopyLabelFamily.getLabelFamilyDescription()+ "mFamilyName: "+ deepCopyLabelFamily.getLabelFamilyName() + " ProjectName: " + deepCopyLabelFamily.getProjectName());
                List<LabelFamily> labelFamiliesProject = labelFamilyRepository.findAllByProjectNameAndOwner(labelFamilyToAddTo.getProjectName(),labelFamilyToAddTo.getOwner());

                boolean labelFamilyAlreadyExists = false;

                for (LabelFamily labelFamilyProject : labelFamiliesProject){
                    if (labelFamilyProject.getLabelFamilyName().equals(deepCopyLabelFamily.getLabelFamilyName()) && labelFamilyProject.getLabelFamilyDescription().equals(deepCopyLabelFamily.getLabelFamilyDescription())) {
                        labelFamilyAlreadyExists = true;
                        System.out.println("samelabelFamilyFound");
                        break;
                    }

                    if (labelFamilyProject.getLabelFamilyName().equals(deepCopyLabelFamily.getLabelFamilyName()) && !labelFamilyProject.getLabelFamilyDescription().equals(deepCopyLabelFamily.getLabelFamilyDescription())){

                        deepCopyLabelFamily.setLabelFamilyName(labelFamilyProject.getProjectName()+ "_" +deepCopyLabelFamily.getLabelFamilyName());
                        deepCopyLabelFamily.setProjectName(labelFamilyToAddTo.getProjectName());
                        labelFamilyRepository.save(deepCopyLabelFamily);
                        //labelFamilyRepository.save(labelFamilyToAddDB);
                        labelFamilyAlreadyExists = true;
                        break;
                    }
                }

                if (!labelFamilyAlreadyExists){
                    deepCopyLabelFamily.setProjectName(labelFamilyToAddTo.getProjectName());
                    System.out.println("deepCopy: Description: " + deepCopyLabelFamily.getLabelFamilyDescription()+ " FamilyName: "+ deepCopyLabelFamily.getLabelFamilyName() + " ProjectName: " + deepCopyLabelFamily.getProjectName());
                    System.out.println("labelFamilyToAdd: Description: " + labelFamilyToAddDB.getLabelFamilyDescription()+ " FamilyName: "+ labelFamilyToAddDB.getLabelFamilyName() + " ProjectName: " + labelFamilyToAddDB.getProjectName());
                    labelFamilyRepository.save(deepCopyLabelFamily);
                    labelFamilyRepository.flush();
                    Optional<LabelFamily> labelFamilyForIdOpt =  labelFamilyRepository.findByOwnerAndProjectNameAndLabelFamilyName(deepCopyLabelFamily.getOwner(), deepCopyLabelFamily.getProjectName(), deepCopyLabelFamily.getLabelFamilyName());
                    if (labelFamilyForIdOpt.isPresent()){
                        LabelFamily labelFamilyForId = labelFamilyForIdOpt.get();
                        List <Label> labelsToCopy = labelRepository.findAllByLabelFamilyId(labelFamilyToAddDB.getId());
                        List <Label> labelsDeepCopy = new ArrayList<>();
                        for (Label label : labelsToCopy){
                            Label copiedLabel = deepCopyLabel(label, labelFamilyToAddTo.getProjectName(),labelFamilyForId);
                            labelsDeepCopy.add(copiedLabel);
                        }
                        labelRepository.saveAll(labelsDeepCopy);
                        labelRepository.flush();
                    }

                }
            }
        }
        }

        public void updateLabel(Label label){

            Label newLabel = label;
            System.out.println(newLabel.getLabelName());
            if(newLabel.getLabelName()==null){
                newLabel.setLabelName(" ");
            }
            if(newLabel.getLabelDescription()==null){
                newLabel.setLabelDescription(" ");
            }
            if(newLabel.getIndex()==null){
                newLabel.setIndex("0");
            }
            System.out.println(String.format("NewLabelName: %s", newLabel.getLabelName()));
            Optional<LabelFamily> ownerFamilyOpt = labelFamilyRepository.findByOwnerAndProjectNameAndLabelFamilyName(label.getFamilyOwner(),label.getFamilyProjectName(),label.getFamilyName());

            if (ownerFamilyOpt.isPresent()) {
                LabelFamily ownerFamily = ownerFamilyOpt.get();
                System.out.println(String.format("ownerFamilyName: %s, LabelFamilyId: %s", ownerFamily.getLabelFamilyName(), ownerFamily.getId()));

                String labelNameFromDB = newLabel.getLabelName();

                if(!(newLabel.getOldLabelName().equals(""))){
                    labelNameFromDB = newLabel.getOldLabelName();
                }

                System.out.println(String.format("labelName: %s", labelNameFromDB));

                Optional<Label> existingLabelOpt = labelRepository.findByLabelFamilyIdAndLabelName(ownerFamily.getId(), labelNameFromDB);

                if (existingLabelOpt.isPresent()) {
                    Label existingLabel = existingLabelOpt.get();
                    System.out.println(String.format("NewLabel: Register: %s",newLabel.getRegister()));
                    if (newLabel.getRegister()==true){
                        System.out.println("sameLabelName");
                        throw new ResponseStatusException(HttpStatus.CONFLICT,"Label with name '" + newLabel.getLabelName() + "' already exists.");
                    }

                    checkIfLabelNameUnique(newLabel, ownerFamily.getId());

                    if ((existingLabel.getLabelName().equals(newLabel.getLabelName())) && newLabel.getRegister() == true) {
                        System.out.println("Same labelName");
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "The name of a label must be unique in a project.");
                    }


                    boolean labelChanged = checkIfLabelChanged(existingLabel, newLabel);

                    if (labelChanged) {

                        System.out.println(String.format("Update: NewLabel; Description: %s, Name: %s", newLabel.getLabelDescription(), newLabel.getLabelName()));

                        // Update the label if there are changes
                        updateLabel(existingLabel, newLabel);
                        labelRepository.save(existingLabel);  // Save the updated label
                        }

                } else {
                    System.out.println(String.format("NewLabel; Description: %s, Name: %s, NewLabelLabelFamilyId %s", newLabel.getLabelDescription(), newLabel.getLabelName(), ownerFamily.getId()));

                    newLabel.setLabelFamily(ownerFamily);  // Set the relationship
                    labelRepository.save(newLabel);  // Save the new label
                }
            }else{

                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can't make a label without a corresponding labelFamily!!");

            }
    }
    public List<LabelFamily> getLabelFamilies(LabelFamily labelFamily){
        return labelFamilyRepository.findAllByProjectNameAndOwner(labelFamily.getProjectName(), labelFamily.getOwner());
    }

    public List <Label> getLabels(LabelFamily labelFamily){
        Optional <LabelFamily> labelFamilyDB = labelFamilyRepository.findByOwnerAndProjectNameAndLabelFamilyName(labelFamily.getOwner(),labelFamily.getProjectName(),labelFamily.getLabelFamilyName());
        if (labelFamilyDB.isPresent()){
            return labelRepository.findAllByLabelFamilyId(labelFamilyDB.get().getId());
        }
        throw new ResponseStatusException(HttpStatus.CONFLICT, "The name of a Label Family not found.");
    }

    @Transactional
    public void deleteLabelFamily(LabelFamily labelFamily) {
        System.out.println("startingDeleting");
        Optional <LabelFamily> repositoryLabelFamilyOpt = labelFamilyRepository.findByOwnerAndProjectNameAndLabelFamilyName(labelFamily.getOwner(), labelFamily.getProjectName(), labelFamily.getLabelFamilyName());
        if (repositoryLabelFamilyOpt.isPresent()){
            LabelFamily repositoryLabelFamily = repositoryLabelFamilyOpt.get();

            // 2. Retrieve all associated Labels using the labelFamilyId
            List<Label> associatedLabels = labelRepository.findAllByLabelFamilyId(repositoryLabelFamily.getId());

            for (Label label : associatedLabels) {
                labelRepository.delete(label);
            }

            labelFamilyRepository.delete(repositoryLabelFamily);


        }else{
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Requested Label Family does not exist!");

        }
    }
    public void deleteLabel(Label label){
        System.out.println("arrived in deleteLabel");
        Optional <LabelFamily> labelFamilyOpt = labelFamilyRepository.findByOwnerAndProjectNameAndLabelFamilyName(label.getFamilyOwner(),label.getFamilyProjectName(),label.getFamilyName());
        if (labelFamilyOpt.isPresent()){
            LabelFamily labelFamily = labelFamilyOpt.get();

            Optional <Label> repositoryLabelOpt = labelRepository.findByLabelFamilyIdAndLabelName(labelFamily.getId(),label.getLabelName());
            if (repositoryLabelOpt.isPresent()){
                Label repositoryLabel = repositoryLabelOpt.get();
                labelRepository.delete(repositoryLabel);
            }

        }else{
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Requested Label does not exist!");

        }
    }



    // Helperfunctions
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


    private boolean checkIfFamilyChanged(LabelFamily existingLabelFamily, LabelFamily newLabelFamily) {
        return !existingLabelFamily.getLabelFamilyName().equals(newLabelFamily.getLabelFamilyName()) ||
                !existingLabelFamily.getLabelFamilyDescription().equals(newLabelFamily.getLabelFamilyDescription()) ||
                !existingLabelFamily.getIndex().equals(newLabelFamily.getIndex());
    }


    private void updateLabelFamily(LabelFamily existingLabelFamily, LabelFamily newLabelFamily) {
        existingLabelFamily.setLabelFamilyName(newLabelFamily.getLabelFamilyName());
        existingLabelFamily.setIndex(newLabelFamily.getIndex());
        existingLabelFamily.setLabelFamilyDescription(newLabelFamily.getLabelFamilyDescription());
    }


    private boolean checkIfLabelChanged(Label existingLabel, Label newLabel) {
        return !existingLabel.getLabelName().equals(newLabel.getLabelName()) ||
                !existingLabel.getLabelDescription().equals(newLabel.getLabelDescription()) ||
                !existingLabel.getIndex().equals(newLabel.getIndex());
    }


    private void updateLabel(Label existingLabel, Label newLabel) {
        existingLabel.setLabelName(newLabel.getLabelName());
        existingLabel.setLabelDescription(newLabel.getLabelDescription());
        existingLabel.setIndex(newLabel.getIndex());
    }

    private void checkUniqueUpdatedLabelFamilyName(LabelFamily labelFamily){
        if (labelFamily.getRegister()== false){
            Optional <LabelFamily> familyFromRepo = labelFamilyRepository.findByOwnerAndProjectNameAndLabelFamilyName(labelFamily.getOwner(),labelFamily.getProjectName(),labelFamily.getLabelFamilyName());
            if (familyFromRepo.isPresent()&&!(labelFamily.getLabelFamilyName().equals(labelFamily.getOldLabelFamilyName()))){
                throw new ResponseStatusException(HttpStatus.CONFLICT,"LabelFamily with name '" + labelFamily.getLabelFamilyName() + "' already exists.");
            }
        }
    }
    private void checkIfLabelNameUnique(Label label, Long labelFamilyId){
        if(label.getRegister()==false){
            Optional <Label> labelFromRepo = labelRepository.findByLabelFamilyIdAndLabelName(labelFamilyId, label.getLabelName());
            if (labelFromRepo.isPresent()&&!label.getLabelName().equals(label.getOldLabelName())){
                throw new ResponseStatusException(HttpStatus.CONFLICT,"Label with name '" + label.getLabelName() + "' already exists.");

            }
        }

    }

}
