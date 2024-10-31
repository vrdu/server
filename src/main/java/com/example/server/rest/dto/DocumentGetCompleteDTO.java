package com.example.server.rest.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentGetCompleteDTO {
    private String documentName;
    private String annotation;
    private String ocrData;


}
