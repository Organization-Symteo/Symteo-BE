# 📊 Symteo-BE 전체 도메인 정적 분석 종합 보고서

> **분석 일시**: 2025-02-07  
> **분석 범위**: 6개 도메인 (`counsel`, `diagnose`, `home`, `report`, `todayMission`, `user`) + Global 인프라  
> **평가 기준**: 1-1 API 설계 완성도 (20점) + 1-2 API 구현 완성도 (정적 분석 가능 범위)

---

## 1. 프로젝트 전체 점수 요약

| 순위 | 도메인 | RESTful (5) | 응답 포맷 (5) | 파라미터 (5) | 에러 핸들링 (5) | **합계 (20)** |
|---|---|---|---|---|---|---|
| 🥇 | **Diagnose** | 5.0 | 5.0 | 3.5 | 4.0 | **17.5** |
| 🥈 | **Report** | 3.5 | 5.0 | 4.5 | 3.0 | **16.0** |
| 🥉 | **Counsel** | 3.5 | 4.0 | 3.0 | 4.0 | **14.5** |
| 4위 | **User** | 3.5 | 4.0 | 3.5 | 3.5 | **14.5** |
| 5위 | **Home** | 3.5 | 4.5 | 3.0 | 3.0 | **14.0** |
| 6위 | **TodayMission** | 2.5 | 5.0 | 3.0 | 3.0 | **13.5** |
| | **전체 평균** | **3.6** | **4.6** | **3.4** | **3.4** | **15.0** |

---

## 2. 핵심 문제 6가지 — 우선순위 순 (평가 배점 기준)

> **배점 가중치 참고**  
> 1-1 API 설계 완성도: **20점** / 1-2 API 구현 완성도: **40점** → 합계 **60점**  
> 2-보안: **5점** / 3-DB: **10점** / 4-협업: **10점**  
> → **설계+구현이 전체의 60%**이므로, 이 영역에 집중하는 것이 가장 효율적이다.

---

### 🔴 1순위. 예외 처리 미흡 + HTTP 상태 코드 오용

> **영향 배점**: 1-1 에러 핸들링 **5점** + 1-2 에러 핸들링 확인 **15점** = **최대 20점**  
> **수정 난이도**: ⭐ 낮음~중간

Swagger 테스트에서 비정상 요청을 보낼 때 **올바른 에러 응답이 나오는지**가 1-2의 핵심이다(15점). 현재 여러 도메인에서 500 에러가 발생하거나 잘못된 상태 코드를 반환하여, 설계 5점 + 구현 15점에서 대폭 감점될 수 있다.

#### 주요 이슈

| 도메인 | 이슈 | 현재 상태 | 올바른 처리 |
|---|---|---|---|
| Report | AI API 에러를 삼키고 에러 문자열을 **리포트 본문에 저장** | 사용자에게 에러 메시지가 분석 결과로 노출 | `GeneralException` throw |
| Report | `StressReportsService.getReportDetail()` — `orElse(null)` 후 `.getStressScore()` | **NPE → 500 에러** (Swagger 테스트에서 즉시 발견) | `orElseThrow()` 사용 |
| User/Auth | `AuthService.reissue()` — `IllegalArgumentException` 직접 throw | `ExceptionAdvice`의 일반 핸들러에 잡혀 **500 응답** | `GeneralException(ErrorStatus._UNAUTHORIZED)` |
| Diagnose | `getAllDiagnose()` — 빈 리스트를 `_DIAGNOSE_NOT_FOUND` 예외 처리 | 신규 사용자가 **404를 받음** (정상 동작이 아님) | 빈 리스트 정상 반환 |
| User/Auth | `SocialType.from()` — `IllegalArgumentException` 직접 throw | 일반 핸들러 → **500 응답** | `GeneralException` 사용 |

#### HTTP 상태 코드 오용

| 도메인 | 현재 사용 | 올바른 코드 | 이유 |
|---|---|---|---|
| Report | `401 UNAUTHORIZED` | **`403 FORBIDDEN`** | 인증은 완료, 권한(소유권)이 없는 상태 |
| Counsel | `408 REQUEST_TIMEOUT` | **`502 BAD_GATEWAY`** | 외부 AI 서버 오류 |
| User/Auth | `500 INTERNAL_SERVER_ERROR` | **`401 UNAUTHORIZED`** | 토큰 검증 실패는 인증 오류 |

---

### 🔴 2순위. `@Valid` 전면 미적용 (비정상 요청 처리)

> **영향 배점**: 1-1 파라미터 처리 **5점** + 1-2 에러 핸들링 확인 **15점** = **최대 20점**  
> **수정 난이도**: ⭐ 낮음 (기계적 추가)

1-2 에러 핸들링 확인(15점)의 평가 설명이 **"비정상 요청 시 올바른 에러 응답 제공 (잘못된 파라미터, 미인증 요청 등)"**이다. `@Valid`가 없으면 잘못된 파라미터가 그대로 통과하여 **이 15점에서 대폭 감점**된다.

`ExceptionAdvice`에 `MethodArgumentNotValidException` 핸들러가 **이미 구현되어 있으므로**, `@Valid`만 추가하면 즉시 동작한다.

#### 도메인별 현황

| 도메인 | `@Valid` 적용 | 비정상 데이터 통과 사례 |
|---|---|---|
| Counsel | ❌ | 빈 `text`가 AI에 전송됨 |
| Diagnose | ❌ | `null` testType, 빈 answers, 음수 score 허용 |
| Home | ❌ | `weather` 값 0, -1, 999가 DB에 저장됨 |
| Report | N/A | `@RequestBody` 없음 (PathVariable만 사용) |
| TodayMission | ❌ | 빈 `contents` 임시저장 가능, `UserMissionStartRequest` DTO 미사용 |
| User | ❌ | 닉네임은 Service에서 정규식 검증하지만 `@Valid` 미사용 |

> **6개 도메인 전체에서 `@Valid` 미적용** — 프로젝트 수준의 체계적 누락

---

### 🟡 3순위. API 네이밍 문제 (RESTful 설계)

> **영향 배점**: 1-1 RESTful 원칙 **5점** + 1-2 정상 동작 **20점**(간접)  
> **수정 난이도**: ⭐⭐ 중간 (URL 변경 시 프론트엔드 연동 필요)

| 도메인 | 점수 | 주요 위반 |
|---|---|---|
| TodayMission | **2.5** | `/start`, `/completed`, `/restart` 동사형 3개, PathVariable 혼재 |
| Counsel | 3.5 | `PATCH /counsels` Body에 대상 ID (RPC 스타일) |
| Home | 3.5 | 트레일링 슬래시 `/api/v1/home/` |
| User | 3.5 | `/check-nickname`, `/signup` 동사형, `DELETE`에 `@RequestBody` |
| Report | 3.5 | 동일 위치 PathVariable에 `diagnoseId`/`reportId` 혼재 |
| **Diagnose** | **5.0** | **위반 없음 — 프로젝트 내 모범 사례** |

> Diagnose 도메인이 만점이므로 "못하는 것"이 아니라 **"통일하지 않은 것"**이 핵심이다.

---

### 🟡 4순위. JSON 응답 통일

> **영향 배점**: 1-1 공통 응답 포맷 **5점**  
> **수정 난이도**: ⭐ 낮음 (3~4개 메서드)

| 도메인 | 위반 | 수정 방법 |
|---|---|---|
| Counsel | `saveSettings()` → `ResponseEntity<String>` | `ApiResponse<CounselorSettingsResponse>` 변경 |
| User/Auth | `logout()`, `withdraw()` → `ApiResponse<String>` | 전용 응답 DTO 또는 `ApiResponse<Void>` |
| User/Auth | `DevAuthController` → `ResponseEntity<AuthResponse>` | 파일 삭제 |
| Home | `ApiResponse<Integer>` | 전용 응답 DTO |

> **전체 평균 4.6/5점**으로 이미 잘 되어 있다. 위반 3~4개 메서드에 국한.

---

### 🟡 5순위. 아키텍처 일관성 부재

> **영향 배점**: 4-협업 **10점** + 1-1 설계 완성도에 간접 영향  
> **수정 난이도**: ⭐⭐⭐ 높음 (리팩토링 필요)

6개 도메인이 서로 다른 설계 패턴을 사용하고 있어, 평가자가 **"컨벤션 없이 각자 개발했다"**고 판단할 수 있다.

#### 혼재 현황

| 항목 | 패턴 A | 패턴 B | 패턴 C |
|---|---|---|---|
| **Service 구조** | Command/Query 분리 (Counsel, Diagnose) | 기능별 분리 (Home, Report) | 단일 Service (User ~357줄, TodayMission ~200줄) |
| **에러코드** | 도메인 전용 enum (Counsel, Diagnose) | 글로벌 `ErrorStatus`에 혼재 (나머지 4개 도메인, 26개 코드) | — |
| **`@Transactional`** | 클래스 레벨 (User, Report) | 메서드 레벨 선택 적용 (Home, TodayMission) | 미적용 (Diagnose CommandServiceImpl) |
| **DTO 스타일** | Java `record` (User) | `@Getter` class (Counsel, Diagnose) | Inner static class (Report 13개) |
| **AI 클라이언트** | Spring AI `ChatClient` (Counsel) | `RestTemplate` OpenAI 직접 호출 (Report) | — |
| **`createdAt`** | `@CreationTimestamp` (User) | `@PrePersist` (Report) | `LocalDateTime.now()` 직접 호출 (Home) |

---

### 🟢 6순위. 보안 취약점

> **영향 배점**: 2-보안 **5점**  
> **수정 난이도**: ⭐ 낮음 (몇 줄 수정)

평가 기준에서 보안은 5점(전체의 5%)이므로 설계·구현보다 후순위이다. 다만 수정이 매우 쉬우므로 시간이 남으면 반드시 처리할 것.

#### 발견된 취약점

| 심각도 | 이슈 | 위치 | 영향 범위 |
|---|---|---|---|
| 🔴 Critical | `DevAuthController` 프로덕션 잔존 — `GET /api/v1/dev/login?userId=1`로 **임의 사용자 토큰 발급 가능** | `global/auth/controller/DevAuthController.java` | **전 도메인** |
| 🔴 Critical | `JwtAuthenticationFilter`에서 Role 미검증 — JWT에 `role` claim이 있으나 빈 권한 목록(`new ArrayList<>()`)으로 Authentication 생성. **GUEST도 모든 API 접근 가능** | `global/jwt/JwtAuthenticationFilter.java` | **전 도메인** |
| 🔴 Critical | `DELETE /auth/withdraw`에서 `@RequestBody { userId }`로 **타인 계정 탈퇴 가능** — `@AuthenticationPrincipal` 미사용 | `AuthController.java` + `AuthService.java` | User |
| 🔴 High | `saveCompletedMission()`, `saveDraft()`에서 **소유권 미검증** — `userMissionId`로 조회만 하고 `userId` 일치 확인 없음 | `MissionService.java` | TodayMission |
| 🔴 High | `AttachmentReportsService.getReportDetail()`에서 **소유권 미검증** — 타인의 리포트 열람 가능 | `AttachmentReportsService.java` | Report |
| 🟡 Medium | `saveSettings()`에서 Body의 `userId`로 처리 — `@AuthenticationPrincipal` 미사용으로 타인 설정 덮어쓰기 가능 | `CounselController.java` | Counsel |

> ⚠️ 단, 보안 문제 중 일부는 **1-2 정상 동작 여부(20점)**의 Swagger 테스트에서도 발견될 수 있으므로, 소유권 검증 누락 등은 구현 완성도에도 간접 영향을 준다.

---

## 3. 수정 효율 분석 — 평가 배점 기준 ROI

| 순위 | 이슈 | 수정 시간 | 영향 배점 | 회수 점수 | ROI |
|---|---|---|---|---|---|
| 1 | 예외 처리 + HTTP 코드 | 1시간 | 1-1(5) + 1-2(15) = 20점 | ~12점 | 🔥🔥🔥 최고 |
| 2 | `@Valid` 일괄 추가 | 30분 | 1-1(5) + 1-2(15) = 20점 | ~8점 | 🔥🔥🔥 최고 |
| 3 | API 네이밍 수정 | 2시간+ | 1-1(5) = 5점 | ~3점 | 🔥 |
| 4 | JSON 응답 통일 | 20분 | 1-1(5) = 5점 | ~2점 | 🔥🔥 |
| 5 | 아키텍처 통일 | 4시간+ | 4-협업(10) = 10점 | ~5점 | 🔥 |
| 6 | 보안 취약점 수정 | 30분 | 2-보안(5) = 5점 | ~3점 | 🔥🔥 |

> **1~2순위를 약 1.5시간 투자로 수정하면, 1-1 + 1-2 합계 60점 중에서 가장 큰 점수 회수가 가능하다.**

---

## 4. 도메인별 상세 보고서

| 도메인 | 파일명 | 점수 |
|---|---|---|
| Counsel | `counsel-convention-v1.md` | 14.5 / 20 |
| Diagnose | `diagnose-convention-v1.md` | 17.5 / 20 |
| Home | `home-convention-v1.md` | 14.0 / 20 |
| Report | `report-convention-v1.md` | 16.0 / 20 |
| TodayMission | `todayMission-convention-v1.md` | 13.5 / 20 |
| User | `user-convention-v1.md` | 14.5 / 20 |

---

## 5. 결론

프로젝트의 **가장 잘 된 부분**은 `ApiResponse<T>` 공통 응답 포맷(평균 4.6/5)이고, **가장 시급한 부분**은 보안 취약점(DevAuth 잔존, Role 미검증, 소유권 미검증)이다.

4순위(아키텍처 통일)는 리팩토링 비용이 크므로, 시간이 허락하는 범위에서 선택적으로 진행하되, 최소한 **"왜 이런 구조를 선택했는지"** 참고 서류에 기술하는 것이 유리하다.
