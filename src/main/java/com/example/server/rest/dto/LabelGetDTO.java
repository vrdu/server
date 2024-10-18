package com.example.server.rest.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LabelGetDTO {
    private String labelName;
    private String oldLabelName;
    private String familyName;
    private boolean register;
    private String labelDescription;
    private String index;
}
