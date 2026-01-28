package com.symteo.domain.report.repository;

import com.symteo.domain.report.entity.Reports;
import com.symteo.domain.report.entity.mapping.DepressionReports;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepressionReportsRepository extends JpaRepository<DepressionReports, Long> {
    Optional<DepressionReports> findByReport(Reports report);
}