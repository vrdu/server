package com.example.server.rest.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomFileDTO {
    private String name;
    private String type;
    private long size;
    private long lastModified;
    private String id;
    private boolean extract;
    private byte[] fileData;
    private String extractionResult;

}


