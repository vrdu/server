package com.example.server.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Entity
@Getter
@Setter
@Table(name = "label",uniqueConstraints = {@UniqueConstraint(columnNames = { "labelName", "labelFamily_id"})})
public class Label implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column
    private String labelName;

    @Column
    private String labelDescription;

    @Column
    private String index;

    @Transient
    private String oldLabelName;

    @Transient
    private Boolean register;

    @ManyToOne
    @JoinColumn(name = "labelFamily_id")
    private LabelFamily labelFamily;
}
