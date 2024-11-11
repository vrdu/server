package com.example.server.rest.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnnotationDTO {
    private String text;
    private String labelName;
    private String familyName;
    private Boolean checked;
}
