package com.symteo.domain.report.repository;

import com.symteo.domain.report.entity.DiagnoseAiReports;
import com.symteo.domain.report.entity.Reports;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AiReportsRepository extends JpaRepository<DiagnoseAiReports, Long> {
    Optional<DiagnoseAiReports> findByReport(Reports report);
}