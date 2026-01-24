package com.symteo.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "Settings")
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "settings_id")
    private Long settingsId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false, unique = true)
    private User user;

    @Column(name = "is_cheer_msg_on", nullable = false)
    private Boolean isCheerMsgOn = true;

    @Column(name = "is_analysis_ready_on", nullable = false)
    private Boolean isAnalysisReadyOn = true;

    @Column(name = "is_monthly_report_on", nullable = false)
    private Boolean isMonthlyReportOn = true;

    @Column(name = "is_lock_enabled", nullable = false)
    private Boolean isLockEnabled = false;

    @Builder
    public UserSettings(User user) {
        this.user = user;
        this.isCheerMsgOn = true;
        this.isAnalysisReadyOn = true;
        this.isMonthlyReportOn = true;
        this.isLockEnabled = false;
    }

    // 토글 상태 업데이트 메서드
    public void updateCheerMsg(Boolean isOn) {
        this.isCheerMsgOn = isOn;
    }

    public void updateAnalysisReady(Boolean isOn) {
        this.isAnalysisReadyOn = isOn;
    }

    public void updateMonthlyReport(Boolean isOn) {
        this.isMonthlyReportOn = isOn;
    }

    public void updateLockEnabled(Boolean isEnabled) {
        this.isLockEnabled = isEnabled;
    }
}

