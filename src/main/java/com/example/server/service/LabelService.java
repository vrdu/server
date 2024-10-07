package com.example.server.service;

import com.example.server.entity.Label;
import com.example.server.entity.LabelFamily;
import com.example.server.repository.LabelFamilyRepository;
import com.example.server.repository.LabelRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class LabelService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);
    private final LabelRepository labelRepository;
    private final LabelFamilyRepository labelFamilyRepository;


    @Autowired
    public LabelService(LabelRepository labelRepository, LabelFamilyRepository labelFamilyRepository) {
        this.labelRepository = labelRepository;
        this.labelFamilyRepository = labelFamilyRepository;
    }

    public void updateLabelFamilies(List<LabelFamily> labelFamilies){
        // Loop through the incoming label families
        for (LabelFamily newLabelFamily : labelFamilies) {
            System.out.println(String.format("should have no id: %s", newLabelFamily.getId()));

            if(newLabelFamily.getLabelFamilyName() == null){
                newLabelFamily.setLabelFamilyName(" ");
            }
            if(newLabelFamily.getLabelFamilyDescription() == null){
                newLabelFamily.setLabelFamilyDescription(" ");
            }

            // Retrieve the existing label family from the repository (using owner, projectName, and id for uniqueness)
            Optional<LabelFamily> existingLabelFamilyOpt = labelFamilyRepository
                    .findByOwnerAndProjectNameAndLabelFamilyName(newLabelFamily.getOwner(), newLabelFamily.getProjectName(), newLabelFamily.getLabelFamilyName());
            //problem: here familyId==null-->never returns existing labelFamilies //solution: change FamilyId to FamilyName and when updating the familyName
            if (existingLabelFamilyOpt.isPresent()) {
                LabelFamily existingLabelFamily = existingLabelFamilyOpt.get();

                // Compare the existing label family with the new one
                boolean familyChanged = checkIfFamilyChanged(existingLabelFamily, newLabelFamily);

                if (familyChanged) {
                    System.out.println("familyChangedTrue");

                    updateLabelFamily(existingLabelFamily, newLabelFamily);
                    labelFamilyRepository.save(existingLabelFamily);
                }

                for (Label newLabel : newLabelFamily.getLabels()) {
                    System.out.println(newLabel.getLabelName());
                    if(newLabel.getLabelName() == null){
                        newLabel.setLabelName(" ");
                    }
                    if(newLabel.getLabelDescription() == null){
                        newLabel.setLabelDescription(" ");
                    }
                    if(newLabel.getIndex() == null){
                        System.out.println("indexNull");
                        newLabel.setIndex("0");
                    }

                    Optional<Label> existingLabelOpt = labelRepository
                            .findByLabelFamilyIdAndLabelName(existingLabelFamily.getId(), newLabel.getLabelName());

                    if (existingLabelOpt.isPresent()) {
                        Label existingLabel = existingLabelOpt.get();

                        // Check if the label needs to be updated
                        boolean labelChanged = checkIfLabelChanged(existingLabel, newLabel);

                        if (labelChanged) {
                            System.out.println(String.format("Update: NewLabel; Description: %s, Name: %s Index: %s", newLabel.getLabelDescription(), newLabel.getLabelName(), newLabel.getIndex()));

                            // Update the label if there are changes
                            updateLabel(existingLabel, newLabel);
                            labelRepository.save(existingLabel);  // Save the updated label
                        }
                    } else {
                        System.out.println(String.format("NewLabel; Description: %s, Name: %s ExistingFamilyName: %s Index: %s", newLabel.getLabelDescription(), newLabel.getLabelName(), existingLabelFamily.getLabelFamilyName(), newLabel.getIndex()));

                        // If label does not exist, add it
                        newLabel.setLabelFamily(existingLabelFamily);  // Set the relationship
                        labelRepository.save(newLabel);  // Save the new label
                    }
                }
            } else {
                System.out.println("familyDoesNotExist");
                System.out.println(String.format("Id: %s, Index: %s, description: %s, familyName: %s, owner: %s, projectName: %s", newLabelFamily.getId(),newLabelFamily.getIndex(), newLabelFamily.getLabelFamilyDescription(),newLabelFamily.getLabelFamilyName(),newLabelFamily.getOwner(),newLabelFamily.getProjectName()));
                // If label family does not exist, add it
                labelFamilyRepository.save(newLabelFamily);  // Save the new label family along with labels (cascade)
            }
        }
    }

    // Function to check if a label family has changed
    private boolean checkIfFamilyChanged(LabelFamily existingLabelFamily, LabelFamily newLabelFamily) {
        return !existingLabelFamily.getLabelFamilyName().equals(newLabelFamily.getLabelFamilyName()) ||
                !existingLabelFamily.getLabelFamilyDescription().equals(newLabelFamily.getLabelFamilyDescription()) ||
                !existingLabelFamily.getIndex().equals(newLabelFamily.getIndex());
    }

    // Function to update an existing label family with new values
    private void updateLabelFamily(LabelFamily existingLabelFamily, LabelFamily newLabelFamily) {
        existingLabelFamily.setLabelFamilyName(newLabelFamily.getLabelFamilyName());
        existingLabelFamily.setIndex(newLabelFamily.getIndex());
        existingLabelFamily.setLabelFamilyDescription(newLabelFamily.getLabelFamilyDescription());
    }

    // Function to check if a label has changed
    private boolean checkIfLabelChanged(Label existingLabel, Label newLabel) {
        return !existingLabel.getLabelName().equals(newLabel.getLabelName()) ||
                !existingLabel.getLabelDescription().equals(newLabel.getLabelDescription()) ||
                !existingLabel.getIndex().equals(newLabel.getIndex());
    }

    // Function to update an existing label with new values
    private void updateLabel(Label existingLabel, Label newLabel) {
        existingLabel.setLabelName(newLabel.getLabelName());
        existingLabel.setLabelDescription(newLabel.getLabelDescription());
        existingLabel.setIndex(newLabel.getIndex());
    }












}
