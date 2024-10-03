package com.example.server.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "label",uniqueConstraints = {@UniqueConstraint(columnNames = { "id", "labelFamily"})})
public class Label {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String labelName;

    private String labelDescription;

    private String index;

    @ManyToOne
    @JoinColumn(name = "label_family_id")
    private LabelFamily labelFamily;
}
