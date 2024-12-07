package com.example.server.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Entity
@Getter
@Setter
@Table(name = "singleExtraction",uniqueConstraints = {@UniqueConstraint(columnNames = {"id"})})
public class SingleExtraction implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column
    private String extractionName;

    @ManyToOne
    @JoinColumn(name = "extractions_id")
    private Extraction extraction;
}

