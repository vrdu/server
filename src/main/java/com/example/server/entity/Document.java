package com.example.server.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "documents", uniqueConstraints = {@UniqueConstraint(columnNames = {"owner", "projectName", "documentName"})})

@Getter
@Setter
public class Document implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String owner;


    @Column(nullable = false, unique = false)
    private String documentName;

    @Column(nullable = false, unique = false)
    private String projectName;

    @Lob
    @Column(nullable = false)
    private byte[] pdfData;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String ocrData;


    @ElementCollection
    private List<BoundingBox> ocrBoxes;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<Annotation> annotations;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String ocrDataAnnotation;

    @Lob
    @Column(columnDefinition = "TEXT",nullable = true)
    private String extractionResult;

    @Column(nullable = true)
    private String annotation;

    @Lob
    @Column(columnDefinition = "TEXT",nullable = true)
    private String prompt;

    @Lob
    @Column(columnDefinition = "TEXT",nullable = true)
    private String extractionSolution;

    @Column(nullable = false)
    private boolean instruction;

    @Column(nullable = true)
    private Double f1;

    @Column(nullable = false)
    private boolean ocrNotPossible;

    @Column(nullable = false)
    private boolean currentlyInOCR;

    @Column(nullable = false)
    private boolean corrected = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;


    public enum Status {
        PENDING,
        PROMPT_GENERATION_IN_PROGRESS,
        PROMPT_COMPLETE,
        EXTRACTION_IN_PROGRESS,
        EXTRACTION_COMPLETE,
        FAILED
    }
    @Override
    public String toString() {
        return "Document{" +
                "id=" + id +
                ", owner='" + owner + '\'' +
                ", documentName='" + documentName + '\'' +
                ", projectName='" + projectName + '\'' +
                ", ocrBoxes=" + ocrBoxes +
                ", annotations=" + annotations +
                ", instruction=" + instruction +
                ", f1=" + f1 +
                ", ocrNotPossible=" + ocrNotPossible +
                ", currentlyInOCR=" + currentlyInOCR +
                ", corrected=" + corrected +
                ", status=" + status +
                '}';
    }

}