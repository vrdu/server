package com.example.server.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "label",uniqueConstraints = {@UniqueConstraint(columnNames = { "id", "labelFamily_id"})})
public class Label {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String labelName;

    private String labelDescription;

    private String index;

    @ManyToOne
    @JoinColumn(name = "labelFamily_id")
    private LabelFamily labelFamily;
}
