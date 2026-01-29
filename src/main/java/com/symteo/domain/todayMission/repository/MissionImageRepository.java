package com.symteo.domain.todayMission.repository;

import com.symteo.domain.todayMission.entity.mapping.MissionImages;
import com.symteo.domain.todayMission.entity.mapping.UserMissions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MissionImageRepository extends JpaRepository<MissionImages, Long> {
    List<MissionImages> findByUserMissions(UserMissions userMissions);
}


