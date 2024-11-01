package com.example.server.rest.dto;


import com.example.server.entity.BoundingBox;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DocumentGetCompleteDTO {
    private String name;
    private String annotation;
    private List<BoundingBox> boxes;
    private String base64PdfData;


}
