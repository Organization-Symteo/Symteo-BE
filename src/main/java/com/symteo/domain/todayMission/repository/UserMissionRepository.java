package com.symteo.domain.todayMission.repository;

import com.symteo.domain.todayMission.entity.mapping.UserMissions;
import com.symteo.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserMissionRepository extends JpaRepository<UserMissions, Long> {
    long countByUser(User user);

    @Query("SELECT um.missions.missionId FROM UserMissions um WHERE um.user.id = :userId AND um.isCompleted = true")
    List<Long> findCompletedMissionIds(@Param("userId") Long userId);

    @Query("SELECT um FROM UserMissions um WHERE um.user.id = :userId AND CAST(um.completedAt AS date) = CAST(CURRENT_DATE AS date)")
    Optional<UserMissions> findTodayMission(@Param("userId") Long userId);

    // 할당된 최신 미션 조회 (날짜 기준)
    Optional<UserMissions> findTopByUserOrderByUserMissionIdDesc(User user);

    // MY 심터 미션 이력 조회
    List<UserMissions> findByUserAndIsCompletedTrueOrderByCompletedAtDesc(User user);

    // 특정 미션 조회 (유저 확인)
    Optional<UserMissions> findByUserMissionIdAndUser(Long userMissionId, User user);

}
