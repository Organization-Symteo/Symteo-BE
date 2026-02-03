package com.symteo.domain.todayMission.entity.mapping;

import com.symteo.domain.todayMission.entity.Missions;
import com.symteo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_missions")
public class UserMissions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "u_mission_id")
    private Long userMissionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    private Missions missions;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "is_completed", nullable = false)
    private boolean isCompleted = false;

    @Column(name = "is_drafted", nullable = false)
    private boolean isDrafted = false;

    @Column(name = "is_restarted", nullable = false)
    private boolean isRestarted = false;

    @Builder
    public UserMissions(User user, Missions missions) {
        this.user = user;
        this.missions = missions;
        this.isCompleted = false;
        this.isDrafted = false;
        this.isRestarted = false; // 초기값 설정
    }

    // 미션 새로고침 메서드
    public void refresh(Missions newMission) {
        this.missions = newMission;
        this.isRestarted = true;
        this.isDrafted = false; // 기존 작성 내용 초기화
    }

    public void markDrafted() { this.isDrafted = true; }
    public void complete() {
        this.isCompleted = true;
        this.completedAt = LocalDateTime.now();
    }
}