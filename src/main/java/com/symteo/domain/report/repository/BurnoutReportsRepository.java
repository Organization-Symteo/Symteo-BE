package com.symteo.domain.report.repository;

import com.symteo.domain.report.entity.Reports;
import com.symteo.domain.report.entity.mapping.BurnoutReports;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BurnoutReportsRepository extends JpaRepository<BurnoutReports, Long> {
    Optional<BurnoutReports> findByReport(Reports report);
}