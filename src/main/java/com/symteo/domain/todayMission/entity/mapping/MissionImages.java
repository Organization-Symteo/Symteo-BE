package com.symteo.domain.todayMission.entity.mapping;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "Mission_images")
public class MissionImages {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "m_image_id")
    private Long missionImageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "u_mission_id", nullable = false)
    private UserMissions userMissions;

    @Column(name = "image_url", columnDefinition = "TEXT", nullable = false)
    private String imageUrl;

    @Builder
    public MissionImages(UserMissions userMissions, String imageUrl) {
        this.userMissions = userMissions;
        this.imageUrl = imageUrl;
    }
}
