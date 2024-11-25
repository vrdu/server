package com.example.server.repository;

import com.example.server.entity.Extraction;
import com.example.server.entity.LabelFamily;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("extractionRepository")
public interface ExtractionRepository extends JpaRepository<Extraction, Long> {
    List<Extraction> findAllByOwnerAndProjectName(String owner, String projectName);


}
