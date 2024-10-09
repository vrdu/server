package com.example.server.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "labelFamily",uniqueConstraints = {@UniqueConstraint(columnNames = {"owner", "projectName", "labelFamilyName"})})

public class LabelFamily implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column
    private String owner;

    @Column
    private String labelFamilyName;

    @Transient
    private String oldLabelFamilyName;

    @Transient
    private Boolean register;

    @Column
    private String projectName;

    @Column
    private String labelFamilyDescription;

    @Transient
    private String oldLabelFamilyName;

    @Transient
    private Boolean register;

    @Column
    private String index;

    @OneToMany(mappedBy = "labelFamily", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Label> labels;
}
