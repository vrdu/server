package com.example.server.repository;

import com.example.server.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("documentRepository")
public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findAllByProjectNameAndOwner(String projectName, String owner);
    Optional<Document> findByOwnerAndProjectNameAndDocumentName(String owner, String projectName, String documentName);


}

