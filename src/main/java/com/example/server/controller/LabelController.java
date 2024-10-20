package com.example.server.controller;

import com.example.server.entity.Document;
import com.example.server.entity.Label;
import com.example.server.entity.LabelFamily;
import com.example.server.rest.dto.*;
import com.example.server.rest.mapper.DTOMapper;
import com.example.server.service.LabelService;
import com.example.server.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class LabelController {
    private final UserService userService;
    private final LabelService labelService;

    public LabelController(UserService userService, LabelService labelService) {
        this.userService = userService;
        this.labelService = labelService;
    }

    @PostMapping("/projects/{username}/{projectName}/label-families")
    //to make it unique the projectName is a concatenation of username and projectName they are seperated by &
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<String> uploadLabelFamilies(
            @PathVariable String projectName,
            @PathVariable String username,
            HttpServletRequest request,
            @RequestBody LabelFamilyPostDTO labelFamilyPostDTO) throws IOException {
        System.out.println("arrived");
        userService.validateToken(request);


        LabelFamily labelFamily = DTOMapper.INSTANCE.convertLabelFamilyPostDTOToEntity(labelFamilyPostDTO);
        labelFamily.setOwner(username);
        labelFamily.setProjectName(projectName);
        System.out.println("Entpacken...");
        System.out.println(String.format("LabelController FamilyId: %s", labelFamily.getId()));
        System.out.println(String.format("LabelController FamilyInUse %s", labelFamilyPostDTO.getInUse()));


        labelService.updateLabelFamily(labelFamily);

        return ResponseEntity.ok("File uploaded successfully");
    }

/*
    @GetMapping("/projects/{username}/{projectName}/label-families") //to make it unique the projectName is a concatenation of username and projectName they are seperated by &
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<List<LabelFamilyNameGetDTO>> getLabelFamilies(
            @PathVariable String projectName,
            @PathVariable String username,
            HttpServletRequest request) throws IOException {

        userService.validateToken(request);


        LabelFamily labelFamily = new LabelFamily();
        labelFamily.setOwner(username);
        labelFamily.setProjectName(projectName);

        // Fetch the list of LabelFamily entities
        List<LabelFamily> labelFamiliesDB = labelService.getLabelFamilies(labelFamily);

// Convert the list of LabelFamily entities to a list of LabelFamilyNameGetDTOs
        List<LabelFamilyNameGetDTO> labelFamilyNameGetDTOs = labelFamiliesDB.stream()
                .map(DTOMapper.INSTANCE::convertEntityToLabelFamilyNameGetDTO) // Use the appropriate conversion method
                .toList();

        return ResponseEntity.ok(labelFamilyNameGetDTOs);
    }*/
    @GetMapping("/projects/{username}/{projectName}/label-families") //to make it unique the projectName is a concatenation of username and projectName they are seperated by &
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<LabelFamilyGetDTO> getLabelFamilies(
            @PathVariable String projectName,
            @PathVariable String username,
            HttpServletRequest request) throws IOException {

        userService.validateToken(request);
        LabelFamily labelFamily = new LabelFamily();
        labelFamily.setOwner(username);
        labelFamily.setProjectName(projectName);


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


        System.out.println(String.format("returned labelFamilies in getLabelFamiles: %s", labelFamiliesGetDTO));
        return labelFamiliesGetDTO;
    }

    @PostMapping("/projects/{username}/{projectName}/labels") //to make it unique the projectName is a concatenation of username and projectName they are seperated by &
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<String> uploadLabel(
            @PathVariable String projectName,
            @PathVariable String username,
            HttpServletRequest request,
            @RequestBody LabelPostDTO labelPostDTO) throws IOException {

        System.out.println("arrived");
        userService.validateToken(request);


        Label label = DTOMapper.INSTANCE.convertLabelPostDTOToEntity(labelPostDTO);
        label.setFamilyOwner(username);
        label.setFamilyProjectName(projectName);


        labelService.updateLabel(label);
        return ResponseEntity.ok("File uploaded successfully");
    }

    @GetMapping("/projects/{username}/{projectName}/{familyName}/labels") //to make it unique the projectName is a concatenation of username and projectName they are seperated by &
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<List<LabelNameGetDTO>> getLabels(
            @PathVariable String projectName,
            @PathVariable String username,
            @PathVariable String familyName,
            HttpServletRequest request) throws IOException {

        userService.validateToken(request);


        LabelFamily labelFamily = new LabelFamily();
        labelFamily.setOwner(username);
        labelFamily.setProjectName(projectName);
        labelFamily.setLabelFamilyName(familyName);

        // Fetch the list of LabelFamily entities
        List<Label> labelsDB = labelService.getLabels(labelFamily);

// Convert the list of LabelFamily entities to a list of LabelFamilyNameGetDTOs
        List<LabelNameGetDTO> labelFamilyNameGetDTOs = labelsDB.stream()
                .map(DTOMapper.INSTANCE::convertEntityToLabelGetDTOTo) // Use the appropriate conversion method
                .toList();

        return ResponseEntity.ok(labelFamilyNameGetDTOs);
    }
}
