package com.example.server.rest.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectUpdatePostDTO {
    private String projectName;
    private String labelFamilies;
    private boolean toImport;
}
