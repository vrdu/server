package com.example.server.rest.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LabelFamilyUpdatePostDTO {
    private String labelFamilyName;
    private String projectName;
    private boolean toImport;
}
