package com.example.server.repository;

import com.example.server.entity.LabelFamily;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("labelFamilyRepository")
public interface LabelFamilyRepository extends JpaRepository<LabelFamily, Long> {
    List<LabelFamily> findAllByProjectNameAndOwner(String projectName, String owner);

    Optional<LabelFamily> findByOwnerAndProjectNameAndId(String owner, String projectName, Long id);
}




