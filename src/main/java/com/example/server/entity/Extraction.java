package com.example.server.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "extractions", uniqueConstraints = {@UniqueConstraint(columnNames = {"owner", "projectName", "extractionName"})})

@Getter
@Setter
public class Extraction {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String owner;

    @Column(nullable = false)
    private String projectName;

    @Column(nullable = false)
    private String extractionName;

    @Column(nullable = false)
    private boolean extractionInProgress;

    //We are in the document level, not the extractions of a document!
    @OneToMany(mappedBy = "extraction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SingleExtraction> extractions;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Document.Status status = Document.Status.PENDING;

    public void setStatus(Status status) {
    }

    public enum Status {
        PENDING,
        PROMPT_GENERATION_IN_PROGRESS,
        PROMPT_COMPLETE,
        EXTRACTION_IN_PROGRESS,
        EXTRACTION_COMPLETE,
        FAILED
    }
}
