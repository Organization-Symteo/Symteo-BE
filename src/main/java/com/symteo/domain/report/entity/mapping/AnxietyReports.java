package com.symteo.domain.report.entity.mapping;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.symteo.domain.report.entity.Reports;
import com.symteo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "Anxiety_reports")
public class AnxietyReports {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "an_report_id")
    private Long anReportId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private User user;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)

    private Reports report;
    private Integer anxietyScore;
    private String severity;
    private Integer emotionalScore; // 정서적 불안 (1, 2, 3번)
    private Integer tensionScore;   // 신체적 긴장 (4, 5, 6, 7번)
}