package com.example.server.rest.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LabelPostDTO {
    private String labelName;
    private String oldLabelName;
    private boolean register;
    private Boolean inUse;
    private String labelDescription;
    private String index;
}
