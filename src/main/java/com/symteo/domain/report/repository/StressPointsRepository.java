package com.symteo.domain.report.repository;

import com.symteo.domain.report.entity.mapping.StressPoints;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StressPointsRepository extends JpaRepository<StressPoints, Long> {
    // 특정 유형(예: 불안형)에 해당하는 스트레스 포인트 2개를 가져옴
    List<StressPoints> findByAttachmentType(String type);
}
