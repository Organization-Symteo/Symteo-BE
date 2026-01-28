package com.symteo.domain.report.repository;

import com.symteo.domain.report.entity.Reports;
import com.symteo.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ReportsRepository extends JpaRepository<Reports, Long> {
    // 리포트 조회(우울/불안)
    @Query("SELECT r FROM Reports r " +
            "LEFT JOIN FETCH r.depressionReport " +
            "LEFT JOIN FETCH r.anxietyReport " +
            "LEFT JOIN FETCH r.aiReport " +
            "WHERE r.reportId = :reportId")
    Optional<Reports> findReportWithDetails(@Param("reportId") Long reportId);

    // 유저, 타입, id가 일치하는 리포트가 있는지 확인
    @Query("SELECT r FROM Reports r WHERE r.user = :user " +
            "AND r.rType = :rType " +
            "AND r.diagnoseId = :diagnoseId")
    Optional<Reports> findByDuplicateCheck(
            @Param("user") User user,
            @Param("rType") String rType,
            @Param("diagnoseId") Long diagnoseId
    );

    // 해당 유저의 이전 리포트 기록 조회
    @Query("SELECT r FROM Reports r WHERE r.user = :user AND r.rType = :rType AND r.createdAt < :createdAt ORDER BY r.createdAt DESC LIMIT 1")
    Optional<Reports> findLastReport(
            @Param("user") User user,
            @Param("rType") String rType,
            @Param("createdAt") LocalDateTime createdAt
    );
}