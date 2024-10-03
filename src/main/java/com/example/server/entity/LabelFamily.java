package com.example.server.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "labelFamily",uniqueConstraints = {@UniqueConstraint(columnNames = {"owner", "id", "projectName"})})

public class LabelFamily {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String owner;

    private String labelFamilyName;


    private String projectName;
    private String labelFamilyDescription;

    private String index;

    @OneToMany(mappedBy = "labelFamily", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Label> labels;
}
