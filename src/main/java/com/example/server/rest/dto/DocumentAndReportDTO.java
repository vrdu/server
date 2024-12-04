package com.example.server.rest.dto;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DocumentAndReportDTO {
    private String name;
    private float f1;
    private float anls;
    private List<String> documentNames;
}
