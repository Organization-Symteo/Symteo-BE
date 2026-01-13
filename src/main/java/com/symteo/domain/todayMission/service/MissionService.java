package com.symteo.domain.todayMission.service;

import com.symteo.domain.todayMission.dto.DraftSaveResponse;
import com.symteo.domain.todayMission.dto.MissionResponse;
import com.symteo.domain.todayMission.dto.UserMissionStartResponse;
import com.symteo.domain.todayMission.entity.Missions;
import com.symteo.domain.todayMission.entity.mapping.Drafts;
import com.symteo.domain.todayMission.entity.mapping.UserMissions;
import com.symteo.domain.todayMission.repository.DraftRepository;
import com.symteo.domain.todayMission.repository.MissionRepository;
import com.symteo.domain.todayMission.repository.UserMissionRepository;
import com.symteo.domain.user.entity.User;
import com.symteo.global.ApiPayload.exception.GeneralException;
import com.symteo.global.ApiPayload.status.ErrorStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MissionService {

    private final MissionRepository missionRepository;
    private final UserMissionRepository userMissionRepository;
    private final DraftRepository draftRepository;

    // 오늘의 미션 조회 api
    public MissionResponse getTodayMission() {

        LocalDateTime startOfToday = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfToday = startOfToday.plusDays(1);

        Missions mission = missionRepository
                .findFirstByCreatedAtBetween(startOfToday, endOfToday)
                .orElseThrow(() -> new GeneralException(ErrorStatus._MISSION_NOT_FOUND));

        long remainingSeconds = Math.max(
                Duration.between(LocalDateTime.now(), mission.getDeadLine()).getSeconds(),
                0
        );

        return MissionResponse.builder()
                .contents(mission.getMissionContents())
                .remainingSeconds(remainingSeconds)
                .isRestarted(mission.isRestarted())
                .build();
    }

    // 오늘의 미션 제출 시작 api
    public UserMissionStartResponse startMission(Long missionId, User user) {

        Missions mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._MISSION_NOT_FOUND));

        // 만료 체크
        if (LocalDateTime.now().isAfter(mission.getDeadLine())) {
            throw new GeneralException(ErrorStatus._MISSION_EXPIRED);
        }

        // 이미 시작한 미션이면 재사용
        UserMissions userMission = userMissionRepository
                .findByUserAndMissions(user, mission)
                .orElseGet(() ->
                        userMissionRepository.save(
                                UserMissions.builder()
                                        .user(user)
                                        .missions(mission)
                                        .build()
                        )
                );

        long remainingSeconds = Duration.between(
                LocalDateTime.now(),
                mission.getDeadLine()
        ).getSeconds();

        return UserMissionStartResponse.builder()
                .userMissionId(userMission.getUserMissionId())
                .isDrafted(userMission.isDrafted())
                .isCompleted(userMission.isCompleted())
                .remainingSeconds(Math.max(remainingSeconds, 0))
                .build();
    }

    // 오늘의 미션 임시저장 api
    @Transactional
    public DraftSaveResponse saveDraft(Long userMissionId, String contents) {
        // 내용이 없는 경우 저장하지 않음
        if (contents == null || contents.isBlank()) {
            return DraftSaveResponse.builder()
                    .isDrafted(false)
                    .build();
        }

        UserMissions userMission = userMissionRepository.findById(userMissionId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._USER_MISSION_NOT_FOUND));

        // 기존 draft 조회 - 임시 저장 여부 조회
        Drafts draft = draftRepository.findTopByUserMissions(userMission)
                .orElse(null);

        if (draft == null) {
            // draft가 없으면 새로 생성 후 저장
            draft = Drafts.builder()
                    .userMissions(userMission)
                    .contents(contents)
                    .build();
            draft = draftRepository.save(draft);
        } else {
            // 기존 draft이면 내용 바로 덮어쓰기
            draft.updateContents(contents);
        }

        // userMission 상태 표시
        userMission.markDrafted();

        return DraftSaveResponse.builder()
                .draftId(draft.getDraftId())
                .isDrafted(true)
                .updatedAt(draft.getUpdatedAt())
                .build();
    }

}
