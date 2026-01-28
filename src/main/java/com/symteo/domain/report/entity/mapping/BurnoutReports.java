package com.symteo.domain.report.entity.mapping;

import com.symteo.domain.report.entity.Reports;
import com.symteo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "burnout_reports")
public class BurnoutReports {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bu_report_id") // DB 컬럼명과 정확히 매핑
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id")
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id")
    private Reports report;

    private Double exhaustionPercent;
    private Double cynicismPercent;
    private Double inefficacyPercent;
    private String exhaustionLevel;
    private String cynicismLevel;
    private String inefficacyLevel;
    private Integer totalBurnoutScore;
    private String totalBurnoutLevel;
}