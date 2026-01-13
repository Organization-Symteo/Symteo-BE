package com.symteo.domain.todayMission.repository;

import com.symteo.domain.todayMission.entity.Missions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface MissionRepository extends JpaRepository<Missions, Long> {
    Optional<Missions> findFirstByCreatedAtBetween(
            LocalDateTime start,
            LocalDateTime end
    );
}