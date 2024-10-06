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
            @RequestBody List<LabelFamilyPostDTO> labelFamilyPostDTOs) throws IOException {
        System.out.println("arrived");
        userService.validateToken(request);

        List<LabelFamily> labelFamilies = new ArrayList<>();

        for(LabelFamilyPostDTO labelFamilyPostDTO : labelFamilyPostDTOs){
            LabelFamily labelFamily = DTOMapper.INSTANCE.convertLabelFamilyPostDTOToEntity(labelFamilyPostDTO);
            labelFamily.setOwner(username);
            labelFamily.setProjectName(projectName);
            System.out.println(String.format("LabelController FamilyId: %s", labelFamily.getId()));

            List<Label> labels = new ArrayList<>();
            for (LabelPostDTO labelPostDTO : labelFamilyPostDTO.getLabels()) {
                Label label = DTOMapper.INSTANCE.convertLabelPostDTOToEntity(labelPostDTO);
                label.setLabelFamily(labelFamily);
                labels.add(label);
            }
            labelFamily.setLabels(labels);

            labelFamilies.add(labelFamily);

        }

        labelService.updateLabelFamilies(labelFamilies);
        return ResponseEntity.ok("File uploaded successfully");
    }
}
