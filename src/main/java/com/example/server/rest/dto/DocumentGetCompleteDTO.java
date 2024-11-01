package com.example.server.rest.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentGetCompleteDTO {
    private String name;
    private String annotation;
    private String ocrData;
    private String base64PdfData;


}
