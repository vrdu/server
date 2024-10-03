package com.example.server.rest.dto;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LabelFamilyPostDTO {
    private Long id;
    private String labelFamilyName;
    private String labelFamilyDescription;
    private String index;
    private List<LabelPostDTO> labels;
}
