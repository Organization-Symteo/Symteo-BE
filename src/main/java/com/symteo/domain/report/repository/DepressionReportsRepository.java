package com.symteo.domain.report.repository;

import com.symteo.domain.report.entity.Reports;
import com.symteo.domain.report.entity.mapping.DepressionReports;
import com.symteo.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepressionReportsRepository extends JpaRepository<DepressionReports, Long> {
    Optional<DepressionReports> findByReport(Reports report);
    // 유저의 가장 최신 우울 리포트 1건 조회
    Optional<DepressionReports> findTopByUserOrderByDeReportIdDesc(User user);
}