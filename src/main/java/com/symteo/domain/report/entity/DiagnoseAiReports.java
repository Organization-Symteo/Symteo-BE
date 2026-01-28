package com.symteo.domain.report.entity;

import com.symteo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Getter @Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "diagnose_ai_reports")
public class DiagnoseAiReports {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diag_ai_id")
    private Long diagnoseAiReportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private Reports report;

    @Column(name = "ai_contents", columnDefinition = "TEXT")
    private String aiContents;
}