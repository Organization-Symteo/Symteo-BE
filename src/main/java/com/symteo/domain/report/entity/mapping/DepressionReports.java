package com.symteo.domain.report.entity.mapping;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.symteo.domain.report.entity.Reports;
import com.symteo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "Depression_reports")
public class DepressionReports {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "de_report_id")
    private Long deReportId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private User user;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)

    private Reports report;
    private Integer depressionScore;
    private String severity;
    private Integer coreScore;     // 핵심 증상 (1, 2번)
    private Integer physicalScore; // 신체 증상 (3, 4, 5번)
    private Integer psychicScore;  // 심리 증상 (6, 7, 8, 9번)
    private Boolean isSafetyFlow;  // 9번 문항 위험 감지
}