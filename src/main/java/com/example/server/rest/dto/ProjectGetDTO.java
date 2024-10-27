package com.example.server.rest.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectGetDTO {
    private String projectName;
    private int f1;
    private int anls;

    @Override
    public String toString() {
        return "ProjectGetDTO{" +
                "projectName='" + projectName + '\'' +
                ", f1=" + f1 +
                ", anls=" + anls +
                '}';

    }
}
