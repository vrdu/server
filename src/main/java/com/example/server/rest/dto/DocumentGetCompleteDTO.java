package com.example.server.rest.dto;


import com.example.server.entity.BoundingBox;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

import com.example.server.entity.BoundingBox;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DocumentGetCompleteDTO {
    private String name;
    private List<HighlightDTO> highlights;
    private List<BoundingBox> boxes;
    private String base64PdfData;

    @Override
    public String toString() {
        return "DocumentSetCompleteDTO{" +
                "name='" + name + '\'' +
                ", highlights=" + highlights +
                ", boxes=" + boxes +
                ", base64PdfData='" + base64PdfData + '\'' +
                '}';
    }
}

