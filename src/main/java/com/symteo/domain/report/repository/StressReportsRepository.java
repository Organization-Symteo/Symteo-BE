package com.symteo.domain.report.repository;

import com.symteo.domain.report.entity.Reports;
import com.symteo.domain.report.entity.mapping.StressReports;
import com.symteo.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StressReportsRepository extends JpaRepository<StressReports, Long> {
    Optional<StressReports> findByReport(Reports report);
    // 유저의 가장 최신 스트레스 리포트 1건 조회
    Optional<StressReports> findTopByUserOrderByStReportIdDesc(User user);
}