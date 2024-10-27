package com.example.server.rest.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class ProjectUpdatePostDTO {
    private String projectName;
    private boolean toImport;
    private List<LabelFamilyUpdatePostDTO> labelFamilies;

}
