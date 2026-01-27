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
    @Column(name = "u_mission_id", nullable = false)
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

    @Builder
    public UserMissions(User user, Missions missions) {
        this.user = user;
        this.missions = missions;
    }

    public void markDrafted() {
        this.isDrafted = true;
    }

    public void complete() {
        this.isCompleted = true;
        this.completedAt = LocalDateTime.now();
    }
}
