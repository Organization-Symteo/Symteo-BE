package com.symteo.domain.todayMission.repository;

import com.symteo.domain.todayMission.entity.Missions;
import com.symteo.domain.todayMission.entity.mapping.UserMissions;
import com.symteo.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserMissionRepository extends JpaRepository<UserMissions, Long> {

    Optional<UserMissions> findByUserAndMissions(User user, Missions missions);
    Optional<UserMissions> findByUserMissionIdAndUser(Long userMissionId, User user);

    // MY 심터 미션 이력 조회
    List<UserMissions> findByUserAndIsCompletedTrueOrderByCompletedAtDesc(User user);

}
