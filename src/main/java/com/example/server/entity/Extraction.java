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

    @OneToMany(mappedBy = "extraction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SingleExtraction> extractions;
}
