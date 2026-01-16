package com.symteo.domain.todayMission.repository;

import com.symteo.domain.todayMission.entity.mapping.MissionImages;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionImageRepository extends JpaRepository<MissionImages, Long> {
}


