package com.symteo.domain.report.repository;

import com.symteo.domain.report.entity.Reports;
import com.symteo.domain.report.entity.mapping.StressReports;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StressReportsRepository extends JpaRepository<StressReports, Long> {
    Optional<StressReports> findByReport(Reports report);
}