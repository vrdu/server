package com.example.server.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Entity
@Table(name = "projects",uniqueConstraints = {@UniqueConstraint(columnNames = {"owner", "projectName"})})
@Getter
@Setter
public class Project {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private int f1;

    @Column(nullable = false)
    private int anls;

    @Column(nullable = false)
    private String projectName;

    @Column(nullable = false)
    private String owner;

}
