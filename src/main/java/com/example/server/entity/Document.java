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

    @ElementCollection
    private List<Annotation> annotations;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String ocrDataAnnotation;

    @Lob
    @Column(nullable = true)
    private String extractionResult;

    @Column(nullable = true)
    private String annotation;

    @Column(nullable = true)
    private String prompt;

    @Column(nullable =false)
    private boolean instruction;

    @Column(nullable =false)
    private boolean ocrNotPossible;

    @Column(nullable =false)
    private boolean currentlyInOCR;
}
