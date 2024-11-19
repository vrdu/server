package com.example.server.rest.dto;


import com.example.server.entity.Document;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DocumentAndExtractionDTO {
    private List<ExtractionGetDTO> extractions;
    private List<DocumentGetDTO> documents;
}
