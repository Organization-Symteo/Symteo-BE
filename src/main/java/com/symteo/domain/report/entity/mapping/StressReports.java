package com.symteo.domain.report.entity.mapping;

import com.symteo.domain.report.entity.Reports;
import com.symteo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Getter @Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "Stress_reports")
public class StressReports {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "st_report_id") // DB 컬럼명과 정확히 매핑
    private Long stReportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id")
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id")
    private Reports report;

    private Integer stressScore;
    private String stressLevel;
    private Double controlPercent;
    private String controlLevel;
    private Double overloadPercent;
    private String overloadLevel;
}