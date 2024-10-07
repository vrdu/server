package com.example.server.repository;

import com.example.server.entity.Label;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("labelRepository")
public interface LabelRepository extends JpaRepository<Label, Long> {

    List<Label> findAllByLabelFamilyId(Long labelFamilyId);
    Optional<Label> findByLabelFamilyIdAndLabelName(Long labelFamilyId, String labelName);



}