# 📋 TodayMission 도메인 — API 평가 기준 정적 분석 보고서

> **분석 대상**: `com.symteo.domain.todayMission`  
> **분석 일시**: 2025-02-07  
> **분석 범위**: 평가 기준 1-1 (API 설계 완성도) + 1-2 (API 구현 완성도 — 정적 분석 가능 범위)

---

## 1-1. API 설계 완성도 (총 20점)

---

### ① RESTful 원칙 준수 (5점)

#### 엔드포인트 목록

| HTTP 메서드 | URL 패턴 | 기능 | RESTful 적합성 |
|---|---|---|---|
| `GET` | `/api/v1/missions/today` | 오늘의 미션 조회 | ✅ |
| `POST` | `/api/v1/missions/{missionId}/start` | 미션 시작 (이미지+글 제출) | ⚠️ 아래 설명 |
| `POST` | `/api/v1/missions/{userMissionId}/draft` | 임시저장 | ⚠️ 아래 설명 |
| `POST` | `/api/v1/missions/{userMissionId}/completed` | 미션 완료 처리 | ⚠️ 아래 설명 |
| `PATCH` | `/api/v1/missions/today-mission/restart` | 미션 새로고침 | ⚠️ 아래 설명 |

#### 상세 분석

**✅ 양호 항목**
- 기본 URL `@RequestMapping("/api/v1/missions")`이 리소스 중심(명사, 복수형)으로 설계됨
- API 버전 관리(`v1`)가 URL에 포함됨
- `@AuthenticationPrincipal`을 모든 엔드포인트에서 일관 사용
- `GET`으로 조회, `POST`로 생성/행위, `PATCH`로 부분 수정 — 메서드 선택 자체는 적절

**⚠️ 개선 필요 항목**

| # | 항목 | 현재 상태 | 권장 사항 | 감점 요인 |
|---|------|-----------|-----------|-----------|
| 1 | **동사형 URL (action-based)** | `/start`, `/completed`, `/restart` | RESTful에서는 동사 사용을 지양. `/api/v1/missions/{missionId}/submissions` (POST), `/api/v1/missions/{userMissionId}/completion` (POST) 형태가 더 리소스 지향적 | 중간 |
| 2 | **PathVariable 의미 혼동** | `POST /{missionId}/start` vs `POST /{userMissionId}/draft` | 동일 위치의 PathVariable이 `missionId`와 `userMissionId`로 **다른 엔티티 ID**를 가리킴. URL만으로 어떤 ID인지 구분 불가 | 중간 |
| 3 | **새로고침 URL 중복 경로** | `PATCH /missions/today-mission/restart` | `/missions` 아래에 `/today-mission`이라는 중복 리소스 경로 존재. `PATCH /missions/today/refresh` 또는 `PATCH /missions/current` 형태가 더 간결 | 경미 |
| 4 | **`/completed` 의미론** | `POST /{userMissionId}/completed` | 완료 처리는 상태 변경이므로 `PATCH`가 더 적절. `POST`는 새 리소스 생성을 의미 | 경미 |
| 5 | **`consumes = MULTIPART_FORM_DATA_VALUE`** | `/start` 엔드포인트만 multipart | `@RequestPart String contents` + `@RequestPart MultipartFile image` 구조. 일반적으로 파일 업로드가 필요한 경우 별도 엔드포인트(`POST /missions/{id}/images`)로 분리하는 것이 RESTful | 경미 |

#### 예상 점수: **2.5 / 5점**

---

### ② 공통 응답 포맷 사용 (5점)

#### 응답 구조 분석

| 엔드포인트 | 반환 타입 | 공통 포맷 준수 |
|---|---|---|
| `GET /missions/today` | `ApiResponse<MissionResponse>` | ✅ |
| `POST /missions/{missionId}/start` | `ApiResponse<UserMissionStartResponse>` | ✅ |
| `POST /missions/{userMissionId}/draft` | `ApiResponse<DraftSaveResponse>` | ✅ |
| `POST /missions/{userMissionId}/completed` | `ApiResponse<UserMissionCompletedResponse>` | ✅ |
| `PATCH /missions/today-mission/restart` | `ApiResponse<MissionResponse>` | ✅ |

**✅ 양호 항목**
- 모든 5개 엔드포인트가 `ApiResponse<T>`를 일관되게 반환
- 각 기능별 전용 응답 DTO 사용 (`MissionResponse`, `UserMissionStartResponse`, `DraftSaveResponse`, `UserMissionCompletedResponse`)
- 새로고침과 조회에 동일한 `MissionResponse` DTO 재사용 — 적절한 설계

#### 예상 점수: **5.0 / 5점**

---

### ③ 파라미터 처리 일관성 (5점)

#### 파라미터 바인딩 분석

| 엔드포인트 | 파라미터 방식 | 적합성 |
|---|---|---|
| `GET /missions/today` | `@AuthenticationPrincipal Long userId` | ✅ |
| `POST /missions/{missionId}/start` | `@PathVariable` + `@AuthenticationPrincipal` + `@RequestPart String` + `@RequestPart MultipartFile` | ⚠️ |
| `POST /missions/{userMissionId}/draft` | `@PathVariable` + `@AuthenticationPrincipal` + `@RequestBody DraftSaveRequest` | ✅ |
| `POST /missions/{userMissionId}/completed` | `@PathVariable` + `@AuthenticationPrincipal` | ✅ |
| `PATCH /missions/today-mission/restart` | `@AuthenticationPrincipal Long userId` | ✅ |

**✅ 양호 항목**
- 모든 엔드포인트에서 `@AuthenticationPrincipal Long userId` 일관 사용
- 임시저장에 `@RequestBody DraftSaveRequest` DTO 사용

**⚠️ 개선 필요 항목**

| # | 항목 | 상세 |
|---|------|------|
| 1 | **`/start`에서 `@RequestPart String contents` 사용** | `@RequestPart`로 단일 문자열을 받는 것은 비표준. multipart 요청에서 텍스트 데이터는 별도 JSON part 또는 `@RequestParam`으로 받는 것이 일반적. 다른 엔드포인트의 `@RequestBody` DTO 패턴과 **불일관** |
| 2 | **`UserMissionStartRequest` DTO 미사용** | `UserMissionStartRequest` DTO가 존재하나 Controller에서 사용되지 않음. `contents`와 `imageUrl` 필드를 가지고 있지만 실제로는 `@RequestPart`로 개별 파라미터를 받음 |
| 3 | **`DraftSaveRequest`에 `@Valid` 미적용** | `contents` 필드에 `@NotBlank` 등 검증 부재. 빈 문자열 임시저장 가능 |
| 4 | **`@Valid` 전면 미적용** | 모든 `@RequestBody`에 `@Valid` 없음 |
| 5 | **`saveCompletedMission()`에서 userId 미활용** | `@AuthenticationPrincipal Long userId`를 받지만 소유권 검증에 사용하지 않음. 타인의 `userMissionId`로 완료 처리 가능 |

#### 예상 점수: **3.0 / 5점**

---

### ④ 에러 핸들링 (5점)

#### 에러 코드 체계

**도메인 전용 에러코드**: ❌ **없음** — `MissionErrorCode` enum이 존재하지 않음

**글로벌 에러코드 (`ErrorStatus`) 활용:**

| 에러 코드 | HTTP 상태 | 사용 위치 |
|---|---|---|
| `_MISSION_NOT_FOUND` | `404 NOT_FOUND` | `getTodayMission()`, `startMission()`, `refreshTodayMission()` |
| `_MISSION_EXPIRED` | `410 GONE` | `startMission()` |
| `_USER_MISSION_NOT_FOUND` | `404 NOT_FOUND` | `startMission()`, `saveDraft()`, `saveCompletedMission()` |
| `_MEMBER_NOT_FOUND` | `404 NOT_FOUND` | 여러 메서드 |
| `_MISSION_REFRESH_EXCEEDED` | `409 CONFLICT` | `refreshTodayMission()` |
| `_MISSION_ALREADY_COMPLETED` | `409 CONFLICT` | `refreshTodayMission()` |
| `_NO_MORE_MISSIONS` | `400 BAD_REQUEST` | `refreshTodayMission()` |
| `_IMAGE_UPLOAD_FAILED` | `400 BAD_REQUEST` | (S3Service 내부에서 사용 추정) |

**✅ 양호 항목**
- 미션 관련 에러코드가 풍부하게 정의됨 (7종 이상)
- 미션 만료(`410 GONE`), 새로고침 초과(`409 CONFLICT`), 이미 완료(`409 CONFLICT`) 등 세분화된 비즈니스 에러 처리
- `@Transactional`이 수정 메서드에 적절히 적용됨

**⚠️ 개선 필요 항목**

| # | 항목 | 상세 |
|---|------|------|
| 1 | **도메인 전용 에러코드 부재** | Counsel, Diagnose 도메인처럼 `MissionErrorCode` enum으로 분리하지 않고, 글로벌 `ErrorStatus`에 미션 관련 코드 7개가 혼재 |
| 2 | **`saveCompletedMission()` 소유권 미검증** | `userId`를 파라미터로 받지만 `userMission.getUser().getId().equals(userId)` 검증 없음. 타인의 미션을 완료 처리할 수 있는 보안 취약점 |
| 3 | **`saveDraft()` 소유권 미검증** | `userId`로 User를 조회하지만, `userMissionId`의 소유자와 일치하는지 검증 안 함 |
| 4 | **`getTodayMission()` 트랜잭션 미설정** | 조회 메서드임에도 `@Transactional(readOnly = true)` 미적용. Lazy Loading 시 `LazyInitializationException` 발생 가능 |
| 5 | **S3 업로드 실패 시 처리 불명확** | `s3Service.upload()` 실패 시 트랜잭션 롤백 여부 및 에러 처리 로직이 MissionService 내부에서 확인되지 않음 |

#### 예상 점수: **3.0 / 5점**

---

### 📊 1-1 종합 점수 (정적 분석 기반 예상)

| 평가 항목 | 배점 | 예상 점수 | 비고 |
|---|---|---|---|
| RESTful 원칙 준수 | 5점 | **2.5점** | 동사형 URL 다수, PathVariable 혼동, 중복 경로 |
| 공통 응답 포맷 사용 | 5점 | **5.0점** | 전 엔드포인트 ApiResponse 통일 |
| 파라미터 처리 일관성 | 5점 | **3.0점** | @RequestPart 비표준, DTO 미사용, @Valid 없음, 소유권 미검증 |
| 에러 핸들링 | 5점 | **3.0점** | 도메인 에러코드 없음, 소유권 검증 누락, 트랜잭션 미설정 |
| **합계** | **20점** | **13.5점** | |

---

## 1-2. API 구현 완성도 — 정적 분석 가능 범위

---

### ① 정상 동작 여부 — 코드 레벨 위험 요소 (20점)

| # | 위험 요소 | 위치 | 심각도 | 상세 |
|---|-----------|------|--------|------|
| 1 | **소유권 검증 누락 (보안)** | `saveCompletedMission()`, `saveDraft()` | 🔴 높음 | `userId`를 받지만 `userMission`의 소유자 일치 여부를 확인하지 않음. 타인의 미션 완료/임시저장 가능 |
| 2 | **`getTodayMission()` LazyInit 위험** | `MissionService` | 🔴 높음 | `@Transactional` 미적용 상태에서 `userMission.getMissions().getMissionContents()` 호출. Lazy Loading 시 `LazyInitializationException` 발생 가능 |
| 3 | **Controller에 불필요한 의존성** | `MissionController` | 🟡 중간 | `UserRepository`를 Controller에서 직접 주입받으나 사용하지 않음. 데드 코드 |
| 4 | **`UserMissionStartRequest` DTO 미사용** | `dto` 패키지 | 🟡 중간 | DTO가 존재하지만 Controller에서 사용되지 않음. 데드 코드이거나 리팩토링 미완료 |
| 5 | **Service 단일 파일 비대화** | `MissionService.java` | 🟡 중간 | 미션 조회, 시작, 임시저장, 완료, 새로고침, 자동 할당까지 모든 로직이 단일 파일에 집중 (~200줄). Command/Query 분리 또는 기능별 분리 권장 |
| 6 | **`MissionImages` `@NoArgsConstructor` 보호 레벨** | `MissionImages.java` | 🟢 낮음 | `@NoArgsConstructor`에 `access = PROTECTED` 없음. 다른 엔티티(`Drafts`, `UserMissions`, `Missions`)는 모두 `PROTECTED` 사용 |
| 7 | **매직 넘버** | `determineCategory()`, `generateMissionForUser()` | 🟢 낮음 | `controlPercent <= 68.0`, `n % 3`, `n % 2` 등 하드코딩된 비즈니스 룰 |

### ② 에러 핸들링 확인 — 코드 레벨 분석 (15점)

| 시나리오 | 코드 대응 여부 | 상세 |
|---|---|---|
| 미인증 요청 | ✅ 처리됨 | 모든 엔드포인트에 `@AuthenticationPrincipal` 적용 |
| 존재하지 않는 missionId | ✅ 처리됨 | `missionRepository.findById().orElseThrow()` |
| 존재하지 않는 userMissionId | ✅ 처리됨 | `userMissionRepository.findById().orElseThrow()` |
| 미션 기한 만료 후 시작 | ✅ 처리됨 | `LocalDateTime.now().isAfter(mission.getDeadLine())` → `_MISSION_EXPIRED` |
| 이미 새로고침한 미션 새로고침 | ✅ 처리됨 | `isRestarted()` 검사 → `_MISSION_REFRESH_EXCEEDED` |
| 완료된 미션 새로고침 | ✅ 처리됨 | `isCompleted()` 검사 → `_MISSION_ALREADY_COMPLETED` |
| 더 이상 배정 가능 미션 없음 | ✅ 처리됨 | `findRandomByCategory()` 빈 결과 → `_NO_MORE_MISSIONS` |
| 타인의 미션 완료 처리 | ❌ 미처리 | `saveCompletedMission()`에서 소유권 검증 없음 |
| 타인의 미션 임시저장 | ❌ 미처리 | `saveDraft()`에서 소유권 검증 없음 |
| 빈 contents 임시저장 | ❌ 미처리 | `DraftSaveRequest.contents`에 `@NotBlank` 없음 |
| 이미지 없이 /start 호출 | ✅ 처리됨 | `@RequestPart(required = false)` + `image != null` 검사 |

### ③ 참고 서류 (5점)

> 📌 이 항목은 Notion, 영상, README 등 외부 문서로 평가되므로 정적 분석 범위 밖입니다.

---

## 🔧 발견된 주요 이슈 및 개선 권고

### 🔴 즉시 수정 권장 (감점 직결)

| # | 이슈 | 수정 방법 |
|---|------|-----------|
| 1 | `saveCompletedMission()` 소유권 미검증 | `userMission.getUser().getId().equals(userId)` 검증 추가 또는 `userMissionRepository.findByUserMissionIdAndUser()` 사용 |
| 2 | `saveDraft()` 소유권 미검증 | 동일하게 소유권 검증 추가 |
| 3 | `getTodayMission()` `@Transactional` 미적용 | `@Transactional(readOnly = true)` 추가 |
| 4 | Controller에서 미사용 `UserRepository` 제거 | `MissionController`에서 `UserRepository` 의존성 삭제 |

### 🟡 개선 권장

| # | 이슈 | 수정 방법 |
|---|------|-----------|
| 5 | 동사형 URL 다수 | `/start` → `POST /{missionId}/submissions`, `/completed` → `PATCH /{userMissionId}/status`, `/restart` → `PATCH /today/refresh` |
| 6 | `@RequestPart String contents` → DTO 통일 | `UserMissionStartRequest` DTO를 실제로 활용하거나, multipart DTO 바인딩으로 변경 |
| 7 | `MissionService` 비대화 | `MissionCommandService` + `MissionQueryService` 분리, 또는 `MissionAssignmentService`(자동 할당) 분리 |
| 8 | `DraftSaveRequest`에 `@Valid` + `@NotBlank` | `@NotBlank private String contents;` + Controller에 `@Valid` 추가 |
| 9 | `MissionImages` `@NoArgsConstructor` 보호 | `@NoArgsConstructor(access = AccessLevel.PROTECTED)` 적용 |
| 10 | `UserMissionStartRequest` 데드 코드 제거 | 사용하지 않는 DTO 삭제 또는 리팩토링하여 활용 |

---

## 🔎 교차 도메인 분석에서 발견된 추가 사항

> 타 도메인과의 비교에서 발견된 TodayMission 도메인 관련 추가 인사이트

| # | 항목 | 상세 |
|---|------|------|
| 1 | **타 도메인 Repository 직접 의존** | `MissionService`에서 `StressReportsRepository`, `DepressionReportsRepository`를 직접 주입받아 사용. Report 도메인의 서비스 계층을 거치지 않고 Repository에 직접 접근하여 **도메인 간 결합도 높음**. Report 서비스에 조회 메서드를 추가하고 해당 서비스를 주입받는 것이 적절 |
| 2 | **에러코드 아키텍처 불일관** | Counsel(`CounselErrorCode`), Diagnose(`DiagnoseErrorCode`)는 도메인 전용 에러코드 보유. Home, Report, TodayMission은 글로벌 `ErrorStatus`에 의존. 6개 도메인 중 **3개만 전용 에러코드 미보유** — 프로젝트 전체 아키텍처 통일 필요 |
| 3 | **Service 구조 불일관** | Counsel(`CommandService` + `QueryService` 분리), Diagnose(분리), Home(기능별 서비스 분리). TodayMission은 **단일 `MissionService`에 모든 로직 집중** — counsel/diagnose 패턴과 불일관 |
| 4 | **`@Transactional` 적용 불일관** | Home(`@Transactional(readOnly = true)` 조회에 적용), Report(클래스 레벨 `@Transactional`), Diagnose(미적용). TodayMission은 수정 메서드만 적용하고 조회 메서드(`getTodayMission`)는 미적용 |

> 🆕 **Global 인프라 분석 (전체 도메인 공통 영향)**

| # | 항목 | TodayMission 도메인 영향 |
|---|------|---|
| 5 | **`JwtAuthenticationFilter`에서 Role 미검증** | `GUEST` 사용자도 미션 조회/시작/완료 API 호출 가능. 미션은 회원 전용 기능이어야 함 |
| 6 | **`DevAuthController` 프로덕션 잔존** | 임의 토큰으로 미션 API 접근 가능 — 전 도메인 공통 보안 리스크 |
| 7 | **`MissionDataInitializer`에서 초기 데이터 대량 삽입** | Global `config/MissionDataInitializer`에서 `ApplicationReadyEvent`로 미션 데이터 초기화. `missionsRepository.count() == 0` 조건이지만, 프로덕션에서 DB에 미션이 없으면 매 기동 시 대량 INSERT 발생. Flyway 마이그레이션으로 전환 권장 |
| 8 | **`ErrorStatus` 네이밍 불일관** | Mission 관련 에러코드 중 `_MISSION_NOT_FOUND`는 접두사 有, `COUNSELOR_NOT_FOUND`는 접두사 無. 글로벌 `ErrorStatus`에 26개 에러코드가 혼재하며 네이밍 통일 필요 |

---

## 📊 도메인 간 비교 (전체 6개 도메인 최종)

| 평가 항목 | Counsel | Diagnose | Home | Report | TodayMission | User | 비고 |
|---|---|---|---|---|---|---|---|
| RESTful 원칙 준수 | 3.5 / 5 | **5.0 / 5** | 3.5 / 5 | 3.5 / 5 | 2.5 / 5 | 3.5 / 5 | Diagnose 최고, Mission 최저 |
| 공통 응답 포맷 사용 | 4.0 / 5 | **5.0 / 5** | 4.5 / 5 | **5.0 / 5** | **5.0 / 5** | 4.0 / 5 | 3개 도메인 만점 |
| 파라미터 처리 일관성 | 3.0 / 5 | 3.5 / 5 | 3.0 / 5 | **4.5 / 5** | 3.0 / 5 | 3.5 / 5 | Report 가장 일관적 |
| 에러 핸들링 | **4.0 / 5** | **4.0 / 5** | 3.0 / 5 | 3.0 / 5 | 3.0 / 5 | 3.5 / 5 | 전용 에러코드 보유 도메인이 높음 |
| **합계** | **14.5 / 20** | **17.5 / 20** | **14.0 / 20** | **16.0 / 20** | **13.5 / 20** | **14.5 / 20** | 전체 평균: **15.0 / 20** |

---

## 📝 비고

- 이 보고서는 **소스 코드 정적 분석**만을 기반으로 작성되었습니다.
- TodayMission 도메인은 6개 도메인 중 **가장 낮은 예상 점수**(13.5점)를 기록했으며, RESTful 설계와 보안(소유권 검증)이 주요 감점 원인입니다.
- 1-2 항목의 **정상 동작 여부**(20점)와 **에러 핸들링 확인**(15점)은 실제 Swagger 테스트를 통해 최종 평가되어야 합니다.
- 예상 점수는 평가자의 기준에 따라 달라질 수 있으며, 참고용으로만 활용하시기 바랍니다.
