package com.example.server.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Lob;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class Annotation {

    private int annotationId;
    @Lob
    private String jsonData;

    public Annotation() {}

    public Annotation(String jsonData) {
        this.jsonData = jsonData;
    }
    @Override
    public String toString() {return  jsonData ;}

}
