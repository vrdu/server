package com.example.server.rest.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentDeleteDTO {

    private String owner;

    private String documentName;

    private String projectName;

}
