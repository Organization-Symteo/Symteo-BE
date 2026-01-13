package com.symteo.domain.todayMission.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "Missions")
public class Missions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mission_id")
    private Long missionId;

    @Column(name = "m_contents", columnDefinition = "TEXT")
    private String missionContents;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "dead_line", nullable = false)
    private LocalDateTime deadLine;

    @Column(name = "is_restarted", nullable = false)
    private boolean isRestarted = false;

    @Column(name = "is_drafted", nullable = false)
    private boolean isDrafted = false;

}
