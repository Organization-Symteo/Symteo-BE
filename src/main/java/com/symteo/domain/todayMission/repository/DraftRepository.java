package com.symteo.domain.todayMission.repository;

import com.symteo.domain.todayMission.entity.mapping.Drafts;
import com.symteo.domain.todayMission.entity.mapping.UserMissions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DraftRepository extends JpaRepository<Drafts, Long> {
    Optional<Drafts> findTopByUserMissions(UserMissions userMissions);
}
