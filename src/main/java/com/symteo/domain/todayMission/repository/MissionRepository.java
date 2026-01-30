package com.symteo.domain.todayMission.repository;

import com.symteo.domain.todayMission.entity.Missions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MissionRepository extends JpaRepository<Missions, Long> {
    @Query(value = "SELECT * FROM missions m WHERE m.category = :category " +
            "AND m.mission_id NOT IN (:excludeIds) ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<Missions> findRandomByCategory(@Param("category") String category, @Param("excludeIds") List<Long> excludeIds);

    @Query(value = "SELECT * FROM missions m WHERE m.category = :category ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<Missions> findAnyRandom(@Param("category") String category);
}