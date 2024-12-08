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
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("DocumentAndExtractionDTO{");
        sb.append("extractions=").append(extractions != null ? extractions.toString() : "null");
        sb.append(", documents=").append(documents != null ? documents.toString() : "null");
        sb.append('}');
        return sb.toString();
    }
}
