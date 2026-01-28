package com.symteo.domain.report.entity.mapping;

import com.symteo.domain.report.entity.Reports;
import com.symteo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "attachment_reports")
public class AttachmentReports {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "at_report_id")
    private Long atReportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private Reports report;

    // 1. 계산된 수치 데이터 (1.0 ~ 5.0)
    private Double anxietyScore;   // 애착 불안 점수
    private Double avoidanceScore; // 애착 회피 점수

    // 2. 판정 결과
    private String attachmentType;  // 결정된 유형 (안정, 불안, 거부회피, 공포회피)

    // 3. AI 분석 및 가이드 (텍스트만 저장)
    @Column(columnDefinition = "TEXT")
    private String aiFullContent;

    @Column(columnDefinition = "TEXT")
    private String actionGuideSentence;

    // 4. 유형별 매핑된 상세 포인트 (연관 관계)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "st_point_id_1")
    private StressPoints stressPoint1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "st_point_id_2")
    private StressPoints stressPoint2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "strength_id_1")
    private Strength strength1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "strength_id_2")
    private Strength strength2;
}