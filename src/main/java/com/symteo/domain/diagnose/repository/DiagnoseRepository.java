package com.symteo.domain.diagnose.repository;

import com.symteo.domain.diagnose.entity.Diagnose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DiagnoseRepository extends JpaRepository<Diagnose, Long> {
    Optional<List<Diagnose>> findAllByUserId(Long userId);
    Optional<Diagnose> findById(Long testId);
}
