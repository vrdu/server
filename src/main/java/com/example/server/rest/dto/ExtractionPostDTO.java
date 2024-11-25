package com.example.server.rest.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ExtractionPostDTO {
    private int id;
    private String name;
    private List <String> documentNames;

}
