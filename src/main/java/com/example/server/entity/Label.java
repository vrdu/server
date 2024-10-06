package com.example.server.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Entity
@Getter
@Setter
@Table(name = "label",uniqueConstraints = {@UniqueConstraint(columnNames = { "labelId", "labelFamily_id"})})
public class Label implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column
    private Long labelId;

    @Column
    private String labelName;

    @Column
    private String labelDescription;

    @Column
    private String index;

    @ManyToOne
    @JoinColumn(name = "labelFamily_id")
    private LabelFamily labelFamily;
}
