package com.example.server.repository;

import com.example.server.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("projectRepository")
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Project findByProjectNameAndOwner(String projectName, String owner);

    List<Project> findAllByOwner(String owner);
}
