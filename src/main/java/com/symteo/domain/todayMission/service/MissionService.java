package com.symteo.domain.todayMission.service;

import com.symteo.domain.report.repository.DepressionReportsRepository;
import com.symteo.domain.report.repository.StressReportsRepository;
import com.symteo.domain.todayMission.dto.*;
import com.symteo.domain.todayMission.entity.Missions;
import com.symteo.domain.todayMission.entity.mapping.Drafts;
import com.symteo.domain.todayMission.entity.mapping.MissionImages;
import com.symteo.domain.todayMission.entity.mapping.UserMissions;
import com.symteo.domain.todayMission.repository.DraftRepository;
import com.symteo.domain.todayMission.repository.MissionImageRepository;
import com.symteo.domain.todayMission.repository.MissionRepository;
import com.symteo.domain.todayMission.repository.UserMissionRepository;
import com.symteo.domain.user.entity.User;
import com.symteo.domain.user.repository.UserRepository;
import com.symteo.global.ApiPayload.exception.GeneralException;
import com.symteo.global.ApiPayload.status.ErrorStatus;
import com.symteo.global.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MissionService {

    private final MissionRepository missionRepository;
    private final UserMissionRepository userMissionRepository;
    private final UserRepository userRepository;
    private final DraftRepository draftRepository;
    private final MissionImageRepository missionImageRepository;
    private final S3Service s3Service;

    private final StressReportsRepository stressReportsRepository;
    private final DepressionReportsRepository depressionRepository;

    // 매일 자정 자동으로 미션 생성 및 사용자에게 할당
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void assignDailyMissions() {
        userRepository.findAll().forEach(this::generateMissionForUser);
    }

    // 사용자별 맞춤 미션 할당
    @Transactional
    public void generateMissionForUser(User user) {
        // 상태 판별
        boolean isDA = depressionRepository.findTopByUserOrderByDeReportIdDesc(user)
                .map(r -> !r.getSeverity().equals("정상")).orElse(false);

        boolean isST = stressReportsRepository.findTopByUserOrderByStReportIdDesc(user)
                .map(r -> r.getControlPercent() <= 68.0).orElse(false);

        // 미션 할당 횟수(N) 계산 (주기 판단용)
        long n = userMissionRepository.countByUser(user) + 1;

        // 알고리즘에 따른 카테고리 결정
        String targetCategory = determineCategory(isDA, isST, n);

        // 중복 제외 랜덤 추출
        List<Long> completedIds = userMissionRepository.findCompletedMissionIds(user.getId());

        Missions mission;
        if (completedIds.isEmpty()) {
            // 완료한 미션이 하나도 없다면 바로 전체 랜덤 호출
            mission = missionRepository.findAnyRandom(targetCategory)
                    .orElseThrow(() -> new GeneralException(ErrorStatus._MISSION_NOT_FOUND));
        } else {
            // 완료한 미션이 있다면 중복 제외 쿼리 호출 (없으면 다시 전체 중 랜덤)
            mission = missionRepository.findRandomByCategory(targetCategory, completedIds)
                    .orElseGet(() -> missionRepository.findAnyRandom(targetCategory)
                            .orElseThrow(() -> new GeneralException(ErrorStatus._MISSION_NOT_FOUND)));
        }

        // UserMissions 저장
        userMissionRepository.save(UserMissions.builder()
                .user(user)
                .missions(mission)
                .build());
    }

    private String determineCategory(boolean isDA, boolean isST, long n) {
        if (isDA && isST) { // Case D: 3일 주기
            long r = n % 3;
            if (r == 1) return "BASIC";
            if (r == 2) return "DEP_ANX";
            return "STRESS";
        } else if (isDA) { // Case B: 2일 주기
            return (n % 2 != 0) ? "BASIC" : "DEP_ANX";
        } else if (isST) { // Case C: 2일 주기
            return (n % 2 != 0) ? "BASIC" : "STRESS";
        }
        return "BASIC"; // Case A: 일반
    }

    // 오늘의 미션 조회 api
    public MissionResponse getTodayMission(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._MEMBER_NOT_FOUND));

        UserMissions userMission = userMissionRepository.findTopByUserOrderByUserMissionIdDesc(user)
                .orElseThrow(() -> new GeneralException(ErrorStatus._MISSION_NOT_FOUND));

        Missions mission = userMission.getMissions();

        long remainingSeconds = Math.max(
                Duration.between(LocalDateTime.now(), mission.getDeadLine()).getSeconds(),
                0
        );

        return MissionResponse.from(userMission, remainingSeconds);
    }

    // 오늘의 미션 시작 api
    @Transactional
    public UserMissionStartResponse startMission(Long missionId, Long userId, String contents, MultipartFile image) {
        Missions mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._MISSION_NOT_FOUND));

        if (LocalDateTime.now().isAfter(mission.getDeadLine())) {
            throw new GeneralException(ErrorStatus._MISSION_EXPIRED);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._MEMBER_NOT_FOUND));

        UserMissions userMission = userMissionRepository.findTopByUserOrderByUserMissionIdDesc(user)
                .filter(um -> um.getMissions().getMissionId().equals(missionId))
                .orElseThrow(() -> new GeneralException(ErrorStatus._USER_MISSION_NOT_FOUND));

        if (contents != null && !contents.isBlank()) {
            Drafts draft = draftRepository.findTopByUserMissions(userMission)
                    .orElseGet(() -> draftRepository.save(Drafts.builder().userMissions(userMission).contents(contents).build()));
            draft.updateContents(contents);
            userMission.markDrafted();
        }

        if (image != null && !image.isEmpty()) {
            String imageUrl = s3Service.upload(image, "missions");
            missionImageRepository.save(MissionImages.builder().userMissions(userMission).imageUrl(imageUrl).build());
        }

        return UserMissionStartResponse.builder()
                .userMissionId(userMission.getUserMissionId())
                .isDrafted(userMission.isDrafted())
                .isCompleted(userMission.isCompleted())
                .remainingSeconds(Math.max(Duration.between(LocalDateTime.now(), mission.getDeadLine()).getSeconds(), 0))
                .build();
    }

    // 임시저장 api
    @Transactional
    public DraftSaveResponse saveDraft(Long userMissionId, Long userId, String contents) {
        User user = userRepository.findById(userId).orElseThrow(() -> new GeneralException(ErrorStatus._MEMBER_NOT_FOUND));
        UserMissions userMission = userMissionRepository.findById(userMissionId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._USER_MISSION_NOT_FOUND));

        Drafts draft = draftRepository.findTopByUserMissions(userMission)
                .orElseGet(() -> draftRepository.save(Drafts.builder().userMissions(userMission).contents(contents).build()));

        draft.updateContents(contents);
        userMission.markDrafted();

        return DraftSaveResponse.builder()
                .draftId(draft.getDraftId())
                .isDrafted(true)
                .updatedAt(draft.getUpdatedAt())
                .build();
    }

    // 완료 처리 api
    @Transactional
    public UserMissionCompletedResponse saveCompletedMission(Long userMissionId, Long userId) {
        UserMissions userMission = userMissionRepository.findById(userMissionId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._USER_MISSION_NOT_FOUND));

        userMission.complete();
        return UserMissionCompletedResponse.builder()
                .userMissionId(userMission.getUserMissionId())
                .isCompleted(true)
                .build();
    }

    // 새로고침 api
    @Transactional
    public MissionResponse refreshTodayMission(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._MEMBER_NOT_FOUND));

        // 1. 오늘 할당된 미션 조회
        UserMissions userMission = userMissionRepository.findTopByUserOrderByUserMissionIdDesc(user)
                .orElseThrow(() -> new GeneralException(ErrorStatus._MISSION_NOT_FOUND));

        // 2. 하루 1회 제한 체크 (이미 새로고침 했는지 확인)
        if (userMission.isRestarted()) {
            throw new GeneralException(ErrorStatus._MISSION_REFRESH_EXCEEDED);
        }

        // 3. 완료된 미션은 새로고침 불가
        if (userMission.isCompleted()) {
            throw new GeneralException(ErrorStatus._MISSION_ALREADY_COMPLETED);
        }

        // 4. 현재 카테고리 유지하면서 중복 제외하고 다른 미션 찾기
        String currentCategory = userMission.getMissions().getCategory();
        List<Long> excludeIds = userMissionRepository.findCompletedMissionIds(userId);
        excludeIds.add(userMission.getMissions().getMissionId());

        Missions newMission = missionRepository.findRandomByCategory(currentCategory, excludeIds)
                .orElseThrow(() -> new GeneralException(ErrorStatus._NO_MORE_MISSIONS));

        // 5. 엔티티 업데이트 (is_restarted = true 반영)
        userMission.refresh(newMission);

        long remainingSeconds = Math.max(
                Duration.between(LocalDateTime.now(), newMission.getDeadLine()).getSeconds(),
                0
        );

        return MissionResponse.from(userMission, remainingSeconds);
    }
}