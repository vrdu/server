package com.example.server.rest.dto;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LabelFamilyPostDTO {
    private Boolean inUse;
    private String labelFamilyName;
    private String oldLabelFamilyName;
    private boolean register;
    private String labelFamilyDescription;
    private String index;
    private List<LabelPostDTO> labels;
}
