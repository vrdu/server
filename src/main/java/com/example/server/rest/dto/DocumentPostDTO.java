package com.example.server.rest.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentPostDTO {
    private String documentName;
    private String owner;
    private String projectName;
}
