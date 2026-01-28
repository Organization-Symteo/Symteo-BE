package com.symteo.domain.report.repository;

import com.symteo.domain.report.entity.mapping.Strength;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StrengthRepository extends JpaRepository<Strength, Long> {
    List<Strength> findByAttachmentType(String type);
}
