package com.symteo.domain.report.entity;

import com.symteo.domain.report.entity.mapping.AnxietyReports;
import com.symteo.domain.report.entity.mapping.DepressionReports;
import com.symteo.domain.user.entity.User;
import jakarta.persistence.*; // jakarta로 통일
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter @Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "reports")
public class Reports {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private User user;

    @Column(name = "r_type")
    private String rType;

    @Column(name = "diagnose_id")
    private Long diagnoseId;

    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    @OneToOne(mappedBy = "report", fetch = FetchType.LAZY)
    private DepressionReports depressionReport;

    @OneToOne(mappedBy = "report", fetch = FetchType.LAZY)
    private AnxietyReports anxietyReport;

    @OneToOne(mappedBy = "report", fetch = FetchType.LAZY)
    private DiagnoseAiReports aiReport;

    @PrePersist
    public void prePersist() { this.createdAt = LocalDateTime.now(); }

    public void complete() { this.completedAt = LocalDateTime.now(); }
}