package com.example.server.repository;

import com.example.server.entity.Extraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("extractionRepository")
public interface ExtractionRepository extends JpaRepository<Extraction, Long> {
    List<Extraction> findAllByOwnerAndProjectName(String owner, String projectName);
    List<Extraction> findByStatusIn(List<Extraction.Status> statuses);
    Optional<Extraction> findByOwnerAndProjectNameAndExtractionName(String owner, String projectName, String extractionName);

}
