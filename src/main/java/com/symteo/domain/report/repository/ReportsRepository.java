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
    // 유저, 타입, 생성시간이 일치하는 리포트가 있는지 확인
    @Query("SELECT r FROM Reports r WHERE r.user = :user AND r.rType = :rType AND r.createdAt = :createdAt")
    Optional<Reports> findByDuplicateCheck(
            @Param("user") User user,
            @Param("rType") String rType,
            @Param("createdAt") LocalDateTime createdAt
    );
}