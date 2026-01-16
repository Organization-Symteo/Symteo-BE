package com.symteo.domain.todayMission.entity.mapping;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "Drafts")
public class Drafts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "draft_id")
    private Long draftId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "u_mission_id", nullable = false, foreignKey = @ForeignKey(name = "FK_UM_TO_Drafts"))
    private UserMissions userMissions;

    @Column(name = "dm_contents", columnDefinition = "TEXT", nullable = false)
    private String contents;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Drafts(UserMissions userMissions, String contents) {
        this.userMissions = userMissions;
        this.contents = contents;
    }

    public void updateContents(String contents) {
        this.contents = contents;
    }
}
