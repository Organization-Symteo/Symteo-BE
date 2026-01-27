package com.symteo.domain.report.repository;

import com.symteo.domain.report.entity.Reports;
import com.symteo.domain.report.entity.mapping.AnxietyReports;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnxietyReportsRepository extends JpaRepository<AnxietyReports, Long> {
    Optional<AnxietyReports> findByReport(Reports report);
}