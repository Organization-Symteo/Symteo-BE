package com.symteo.domain.todayMission.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "missions")
public class Missions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mission_id")
    private Long missionId;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "sub_category")
    private String subCategory;

    @Column(name = "m_contents", columnDefinition = "TEXT")
    private String missionContents;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "dead_line", nullable = false)
    private LocalDateTime deadLine;
}
