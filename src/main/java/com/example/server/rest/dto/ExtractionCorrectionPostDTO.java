package com.example.server.rest.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExtractionCorrectionPostDTO {
    private String extractionSolution;

    @Override
    public String toString() {
        return "ExtractionCorrectionPostDTO{" +
                "extractionSolution='" + extractionSolution + '\'' +
                '}';
    }
}