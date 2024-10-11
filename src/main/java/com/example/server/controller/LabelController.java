package com.example.server.controller;

import com.example.server.entity.Document;
import com.example.server.entity.Label;
import com.example.server.entity.LabelFamily;
import com.example.server.rest.dto.DocumentPostDTO;
import com.example.server.rest.dto.LabelFamilyPostDTO;
import com.example.server.rest.dto.LabelPostDTO;
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

@RestController
public class LabelController {
    private final UserService userService;
    private final LabelService labelService;

    public LabelController(UserService userService, LabelService labelService) {
        this.userService = userService;
        this.labelService = labelService;
    }
    @PostMapping("/projects/{username}/{projectName}/label-families") //to make it unique the projectName is a concatenation of username and projectName they are seperated by &
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<String> uploadLabelFamilies(
            @PathVariable String projectName,
            @PathVariable String username,
            HttpServletRequest request,
            @RequestBody LabelFamilyPostDTO labelFamilyPostDTO) throws IOException {

        userService.validateToken(request);

        LabelFamily labelFamily = DTOMapper.INSTANCE.convertLabelFamilyPostDTOToEntity(labelFamilyPostDTO);
        labelFamily.setOwner(username);
        labelFamily.setProjectName(projectName);

        labelService.updateLabelFamily(labelFamily);

        return ResponseEntity.ok("File uploaded successfully");
    }
    @DeleteMapping("/projects/{username}/{projectName}/label-families") //to make it unique the projectName is a concatenation of username and projectName they are seperated by &
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<String> deleteLabelFamilies(
            @PathVariable String projectName,
            @PathVariable String username,
            HttpServletRequest request,
            @RequestBody LabelFamilyPostDTO labelFamilyPostDTO) throws IOException {
        System.out.println("arrived to delete LabelFamily");
        userService.validateToken(request);

        LabelFamily labelFamily = DTOMapper.INSTANCE.convertLabelFamilyPostDTOToEntity(labelFamilyPostDTO);
        labelFamily.setOwner(username);
        labelFamily.setProjectName(projectName);

        labelService.deleteLabelFamily(labelFamily);

        return ResponseEntity.ok("File uploaded successfully");
    }

    @PostMapping("/projects/{username}/{projectName}/labels") //to make it unique the projectName is a concatenation of username and projectName they are seperated by &
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<String> uploadLabel(
            @PathVariable String projectName,
            @PathVariable String username,
            HttpServletRequest request,
            @RequestBody LabelPostDTO labelPostDTO) throws IOException {

        userService.validateToken(request);


        Label label = DTOMapper.INSTANCE.convertLabelPostDTOToEntity(labelPostDTO);
        label.setFamilyOwner(username);
        label.setFamilyProjectName(projectName);


        labelService.updateLabel(label);
        return ResponseEntity.ok("File uploaded successfully");
    }

    @DeleteMapping("/projects/{username}/{projectName}/labels") //to make it unique the projectName is a concatenation of username and projectName they are seperated by &
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<String> deleteLabel(
            @PathVariable String projectName,
            @PathVariable String username,
            HttpServletRequest request,
            @RequestBody LabelPostDTO labelPostDTO) throws IOException {
        System.out.println("arrived at Endpoint");

        userService.validateToken(request);


        Label label = DTOMapper.INSTANCE.convertLabelPostDTOToEntity(labelPostDTO);
        label.setFamilyOwner(username);
        label.setFamilyProjectName(projectName);


        labelService.deleteLabel(label);
        return ResponseEntity.ok("File uploaded successfully");
    }
}
