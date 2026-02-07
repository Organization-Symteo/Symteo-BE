# 📋 User 도메인 — API 평가 기준 정적 분석 보고서

> **분석 대상**: `com.symteo.domain.user` + `com.symteo.global.auth`  
> **분석 일시**: 2025-02-07  
> **분석 범위**: 평가 기준 1-1 (API 설계 완성도) + 1-2 (API 구현 완성도 — 정적 분석 가능 범위)  
> **비고**: User 도메인은 `global/auth`(소셜 로그인·토큰·탈퇴)와 긴밀하게 연결되어 있어 함께 분석합니다.

---

## 1-1. API 설계 완성도 (총 20점)

---

### ① RESTful 원칙 준수 (5점)

#### 엔드포인트 목록

**User 도메인 (`UserController`)**

| HTTP 메서드 | URL 패턴 | 기능 | RESTful 적합성 |
|---|---|---|---|
| `GET` | `/api/v1/users/check-nickname` | 닉네임 중복 확인 | ⚠️ |
| `POST` | `/api/v1/users/signup` | 회원가입 완료 (닉네임 설정) | ⚠️ |
| `GET` | `/api/v1/users/profile` | 프로필 조회 | ✅ |
| `PATCH` | `/api/v1/users/nickname` | 닉네임 수정 | ✅ |
| `GET` | `/api/v1/users/settings` | 환경설정 조회 | ✅ |
| `PATCH` | `/api/v1/users/settings` | 환경설정 수정 | ✅ |
| `GET` | `/api/v1/users/counselor-settings` | AI 상담사 설정 조회 | ✅ |
| `PATCH` | `/api/v1/users/counselor-settings` | AI 상담사 설정 수정 | ✅ |
| `GET` | `/api/v1/users/missions/history` | 완료 미션 리스트 조회 | ✅ |
| `GET` | `/api/v1/users/missions/history/{userMissionId}` | 미션 상세 조회 | ✅ |
| `PATCH` | `/api/v1/users/missions/history/{userMissionId}` | 미션 수정 (내용+이미지) | ✅ |

**Auth 도메인 (`AuthController`)**

| HTTP 메서드 | URL 패턴 | 기능 | RESTful 적합성 |
|---|---|---|---|
| `POST` | `/api/v1/auth/login/{provider}` | 소셜 로그인 | ⚠️ |
| `POST` | `/api/v1/auth/refresh` | 토큰 재발급 | ⚠️ |
| `POST` | `/api/v1/auth/logout` | 로그아웃 | ⚠️ |
| `DELETE` | `/api/v1/auth/withdraw` | 회원 탈퇴 | ⚠️ |

#### 상세 분석

**✅ 양호 항목**
- `@RequestMapping("/api/v1/users/")` 리소스 중심 URL, 복수형, 버전 관리 포함
- `GET /settings`, `PATCH /settings` — 동일 리소스에 HTTP 메서드로 CRUD 구분 (우수)
- `GET /missions/history`, `GET /missions/history/{id}`, `PATCH /missions/history/{id}` — 계층적 리소스 구조
- Auth 엔드포인트도 `api/v1/auth/` 접두사 통일

**⚠️ 개선 필요 항목**

| # | 항목 | 현재 상태 | 권장 사항 | 감점 요인 |
|---|------|-----------|-----------|-----------|
| 1 | **URL 트레일링 슬래시** | `@RequestMapping("/api/v1/users/")` | 트레일링 슬래시(`/`) 제거 → `/api/v1/users`. Spring에서 `/users/` 와 `/users`는 다른 URL로 취급될 수 있음 | 경미 |
| 2 | **`/check-nickname` 동사형** | `GET /users/check-nickname?nickname=xxx` | 동사 대신 `GET /users/nicknames/availability?nickname=xxx` 또는 `HEAD /users/nicknames/{nickname}` | 경미 |
| 3 | **`/signup` 동사형** | `POST /users/signup` | `POST /users` 또는 `POST /users/registration`이 더 리소스 지향적 | 경미 |
| 4 | **Auth 동사형 URL 다수** | `/login`, `/refresh`, `/logout` | Auth 관련 액션은 REST 순수주의와 실무 관행 사이 트레이드오프. 다만 `/auth/tokens` (POST=로그인, PUT=재발급, DELETE=로그아웃) 형태도 가능 | 경미 |
| 5 | **`DELETE /auth/withdraw`에서 `@RequestBody`** | `WithdrawRequest { userId }` | DELETE 메서드에 Request Body를 넣는 것은 HTTP 표준에서 비권장. `@AuthenticationPrincipal`로 userId를 가져오거나 `DELETE /auth/users/{userId}` PathVariable 사용이 적절 | 중간 |

#### 예상 점수: **3.5 / 5점**

---

### ② 공통 응답 포맷 사용 (5점)

#### 응답 구조 분석

**User 도메인**

| 엔드포인트 | 반환 타입 | 공통 포맷 준수 |
|---|---|---|
| `GET /check-nickname` | `ApiResponse<NicknameCheckResponse>` | ✅ |
| `POST /signup` | `ApiResponse<AuthResponse>` | ✅ |
| `GET /profile` | `ApiResponse<UserProfileResponse>` | ✅ |
| `PATCH /nickname` | `ApiResponse<UserProfileResponse>` | ✅ |
| `GET /settings` | `ApiResponse<UserSettingsResponse>` | ✅ |
| `PATCH /settings` | `ApiResponse<UserSettingsResponse>` | ✅ |
| `GET /counselor-settings` | `ApiResponse<CounselorSettingsResponse>` | ✅ |
| `PATCH /counselor-settings` | `ApiResponse<CounselorSettingsResponse>` | ✅ |
| `GET /missions/history` | `ApiResponse<MissionHistoryResponse.MissionListResponse>` | ✅ |
| `GET /missions/history/{id}` | `ApiResponse<MissionHistoryResponse.MissionDetailResponse>` | ✅ |
| `PATCH /missions/history/{id}` | `ApiResponse<UpdateMissionResponse>` | ✅ |

**Auth 도메인**

| 엔드포인트 | 반환 타입 | 공통 포맷 준수 |
|---|---|---|
| `POST /auth/login/{provider}` | `ApiResponse<AuthResponse>` | ✅ |
| `POST /auth/refresh` | `ApiResponse<AuthResponse>` | ✅ |
| `POST /auth/logout` | `ApiResponse<String>` | ⚠️ |
| `DELETE /auth/withdraw` | `ApiResponse<String>` | ⚠️ |

**✅ 양호 항목**
- User 도메인 전 11개 엔드포인트 `ApiResponse<T>` 일관 사용 — **우수**
- 조회/수정 동일 DTO 재사용 (`UserProfileResponse`, `UserSettingsResponse`, `CounselorSettingsResponse`) — 적절한 설계

**⚠️ 개선 필요 항목**

| # | 항목 | 상세 |
|---|------|------|
| 1 | **`ApiResponse<String>` 사용** | `logout`과 `withdraw`에서 `ApiResponse<String>`으로 한국어 문자열 직접 반환 (`"로그아웃 되었습니다."`, `"회원 탈퇴가 완료되었습니다."`). 전용 응답 DTO 없이 raw String 사용 |
| 2 | **`NicknameCheckResponse` Controller 내부 record** | Controller 파일 안에 `record NicknameCheckResponse` 정의. `dto` 패키지로 분리하는 것이 일관적 |
| 3 | **DevAuthController `ResponseEntity<AuthResponse>`** | `DevAuthController.devLogin()`이 `ResponseEntity<AuthResponse>` 반환 — `ApiResponse`가 아닌 `ResponseEntity` 직접 사용. 개발용이지만 일관성 위반 |

#### 예상 점수: **4.0 / 5점**

---

### ③ 파라미터 처리 일관성 (5점)

#### 파라미터 바인딩 분석

| 엔드포인트 | 파라미터 방식 | 적합성 |
|---|---|---|
| `GET /check-nickname` | `@RequestParam String nickname` | ✅ (GET 쿼리 파라미터) |
| `POST /signup` | `@AuthenticationPrincipal` + `@RequestBody UserSignUpRequest` | ✅ |
| `GET /profile` | `@AuthenticationPrincipal Long userId` | ✅ |
| `PATCH /nickname` | `@AuthenticationPrincipal` + `@RequestBody UpdateNicknameRequest` | ✅ |
| `GET /settings` | `@AuthenticationPrincipal Long userId` | ✅ |
| `PATCH /settings` | `@AuthenticationPrincipal` + `@RequestBody UpdateUserSettingsRequest` | ✅ |
| `GET /counselor-settings` | `@AuthenticationPrincipal Long userId` | ✅ |
| `PATCH /counselor-settings` | `@AuthenticationPrincipal` + `@RequestBody UpdateCounselorSettingsRequest` | ✅ |
| `GET /missions/history` | `@AuthenticationPrincipal Long userId` | ✅ |
| `GET /missions/history/{id}` | `@AuthenticationPrincipal` + `@PathVariable Long userMissionId` | ✅ |
| `PATCH /missions/history/{id}` | `@AuthenticationPrincipal` + `@PathVariable` + `@RequestPart` (multipart) | ⚠️ |
| `POST /auth/login/{provider}` | `@PathVariable String provider` + `@RequestBody LoginRequest` | ✅ |
| `POST /auth/refresh` | `@RequestBody RefreshTokenRequest` | ✅ |
| `POST /auth/logout` | `@RequestBody LogoutRequest` | ✅ |
| `DELETE /auth/withdraw` | `@RequestBody WithdrawRequest` | ⚠️ |

**✅ 양호 항목**
- 모든 User 도메인 엔드포인트에서 `@AuthenticationPrincipal Long userId` 일관 사용 (Auth 제외, 인증 이전 단계이므로 적절)
- `@RequestBody` + 전용 DTO 패턴 10/11 엔드포인트에서 일관 적용
- `GET` 조회 시 `@RequestParam`, `@PathVariable` 적절히 구분 사용

**⚠️ 개선 필요 항목**

| # | 항목 | 상세 |
|---|------|------|
| 1 | **`@Valid` 전면 미적용** | `UserSignUpRequest`, `UpdateNicknameRequest`, `UpdateCounselorSettingsRequest` 등 모든 `@RequestBody`에 `@Valid` 없음. 닉네임 검증은 Service에서 직접 수행 — 역할 분리 관점에서 `@Valid` + Bean Validation이 적절 |
| 2 | **`PATCH /missions/history/{id}`에서 `@RequestPart`** | `@RequestPart(required = false) UpdateMissionRequest request` + `@RequestPart(required = false) List<MultipartFile> images`. DTO를 `@RequestPart`로 받는 것은 `Content-Type: multipart/form-data` 전용이며, JSON body와 혼합 사용 시 혼란 유발 |
| 3 | **`WithdrawRequest { userId }`에서 `@AuthenticationPrincipal` 미사용** | 탈퇴 시 userId를 RequestBody로 전달. 토큰에서 추출하는 것이 보안상 적절. 현재 구조에서는 타인의 userId를 넣어 탈퇴시킬 수 있는 보안 취약점 |
| 4 | **`UserSignUpRequest`에 닉네임 외 필드 없음** | DTO에 `@NotBlank` 등 검증 어노테이션 없음. Service에서 직접 정규식 검증 수행 |
| 5 | **Auth DTO에 `@Valid` 미적용** | `LoginRequest`, `RefreshTokenRequest`, `LogoutRequest` 모두 `@Valid` 없음 |

#### 예상 점수: **3.5 / 5점**

---

### ④ 에러 핸들링 (5점)

#### 에러 코드 체계

**도메인 전용 에러코드**: ❌ **없음** — `UserErrorCode` enum이 존재하지 않음

**글로벌 에러코드 (`ErrorStatus`) 활용:**

| 에러 코드 | HTTP 상태 | 사용 위치 |
|---|---|---|
| `_MEMBER_NOT_FOUND` | `404 NOT_FOUND` | 거의 모든 메서드 |
| `_NICKNAME_EMPTY` | `400 BAD_REQUEST` | `checkNicknameDuplication()` |
| `_NICKNAME_INVALID` | `400 BAD_REQUEST` | `checkNicknameDuplication()` |
| `_NICKNAME_CONFLICT` | `409 CONFLICT` | `completeSignUp()`, `updateNickname()` |
| `COUNSELOR_NOT_FOUND` | `404 NOT_FOUND` | `getCounselorSettings()`, `updateCounselorSettings()` |
| `_USER_MISSION_NOT_FOUND` | `404 NOT_FOUND` | `getMissionDetail()`, `updateMission()` |
| `_DRAFT_NOT_FOUND` | `404 NOT_FOUND` | `updateMission()` |
| `_SOCIAL_LOGIN_FAILED` | `500 INTERNAL_SERVER_ERROR` | `getSocialAccessToken()` |
| `_INVALID_PROVIDER` | `400 BAD_REQUEST` | `getSocialAccessToken()` |
| `_WITHDRAWAL_RESTRICTION` | `403 FORBIDDEN` | `processLogin()` |
| `_TOKEN_NOT_FOUND` | `404 NOT_FOUND` | `logout()` |

**✅ 양호 항목**
- 에러 시나리오 풍부 — 닉네임 빈값/형식/중복, 회원 미존재, 상담사 설정 미존재, 소셜 로그인 실패, 탈퇴 유예기간 등 11종 이상
- `UserService`의 닉네임 검증에서 **서비스 계층 내 자체 유효성 검사** (정규식 + 중복 검사) — 방어적 프로그래밍
- `AuthService.processLogin()`에서 탈퇴 7일 유예 검증 — 비즈니스 규칙 적절 구현

**⚠️ 개선 필요 항목**

| # | 항목 | 상세 |
|---|------|------|
| 1 | **도메인 전용 에러코드 부재** | User 관련 에러가 글로벌 `ErrorStatus`에 12개 이상 혼재. `UserErrorCode` 또는 `AuthErrorCode` enum 분리 필요 |
| 2 | **`AuthService.reissue()` — `IllegalArgumentException` 직접 throw** | `GeneralException(ErrorStatus.xxx)` 대신 `new IllegalArgumentException("유효하지 않은 Refresh Token입니다.")` 직접 throw. `ExceptionAdvice`의 일반 `Exception` 핸들러에 잡혀 `500 INTERNAL_SERVER_ERROR`로 반환됨 — **응답 코드 부정확** |
| 3 | **`ErrorStatus` 네이밍 불일관** | `_MEMBER_NOT_FOUND`(언더스코어 접두사) vs `COUNSELOR_NOT_FOUND`(접두사 없음) vs `COUNSELOR_ALREADY_EXISTS`(접두사 없음). 네이밍 컨벤션 통일 필요 |
| 4 | **`WithdrawRequest.userId` 보안 취약점** | `@AuthenticationPrincipal`이 아닌 RequestBody의 userId로 탈퇴 처리. 인증된 사용자와 요청 userId 불일치 가능 |
| 5 | **`DevAuthController` 삭제 미완** | 주석에 "후에 삭제 예정"이지만 프로덕션 코드에 잔존. `SecurityConfig`에서도 `/api/v1/dev/**` 허용 중 — 보안 리스크 |

#### 예상 점수: **3.5 / 5점**

---

### 📊 1-1 종합 점수 (정적 분석 기반 예상)

| 평가 항목 | 배점 | 예상 점수 | 비고 |
|---|---|---|---|
| RESTful 원칙 준수 | 5점 | **3.5점** | 트레일링 슬래시, 동사형 URL, DELETE body |
| 공통 응답 포맷 사용 | 5점 | **4.0점** | User 도메인 만점, Auth에서 String 반환 2건 |
| 파라미터 처리 일관성 | 5점 | **3.5점** | @Valid 미적용, withdraw 보안, @RequestPart DTO |
| 에러 핸들링 | 5점 | **3.5점** | IllegalArgumentException 혼용, 도메인 에러코드 없음, DevAuth 잔존 |
| **합계** | **20점** | **14.5점** | |

---

## 1-2. API 구현 완성도 — 정적 분석 가능 범위

---

### ① 정상 동작 여부 — 코드 레벨 위험 요소 (20점)

| # | 위험 요소 | 위치 | 심각도 | 상세 |
|---|-----------|------|--------|------|
| 1 | **`withdraw()` 보안 취약점** | `AuthController` + `AuthService` | 🔴 높음 | `WithdrawRequest.userId`를 RequestBody에서 받아 탈퇴 처리. `@AuthenticationPrincipal`로 토큰에서 추출하지 않으므로, **인증된 사용자가 타인의 userId를 넣어 다른 유저를 탈퇴시킬 수 있음** |
| 2 | **`reissue()`에서 `IllegalArgumentException` 직접 throw** | `AuthService.java` | 🔴 높음 | `jwtProvider.validateToken()` 실패 시 `IllegalArgumentException` throw → `ExceptionAdvice.exception()` 핸들러 → `500 INTERNAL_SERVER_ERROR` + 에러 메시지 노출. `GeneralException(ErrorStatus._UNAUTHORIZED)` 사용이 적절 |
| 3 | **`DevAuthController` 프로덕션 잔존** | `DevAuthController.java` | 🔴 높음 | `GET /api/v1/dev/login?userId=1`로 임의 사용자의 토큰 발급 가능. `SecurityConfig`에서 `/api/v1/dev/**` permitAll 설정. 프로덕션 배포 시 **심각한 보안 취약점** |
| 4 | **`updateCounselorSettings()` 비효율적 엔티티 재생성** | `UserService.java` | 🟡 중간 | 주석에 "엔티티에 업데이트 메서드x → 새로 생성해서 저장하기..."로 명시. 기존 엔티티를 수정하지 않고 `Builder`로 새 객체를 만들어 `save()` — Dirty Checking 미활용, 불필요한 INSERT/UPDATE 발생 가능 |
| 5 | **`UserService` 비대화 (SRP 위반)** | `UserService.java` | 🟡 중간 | 닉네임 관리, 프로필, 설정, 상담사 설정, 미션 히스토리, 미션 수정까지 ~357줄 단일 파일. `UserProfileService`, `UserSettingsService`, `MissionHistoryService` 등으로 분리 권장 |
| 6 | **`JwtAuthenticationFilter`에서 Role 기반 권한 미검증** | `JwtAuthenticationFilter.java` | 🟡 중간 | `new ArrayList<>()` (빈 권한 목록)으로 `Authentication` 생성. JWT에 `role` claim이 있으나 Filter에서 추출하지 않음. `GUEST` 권한 사용자가 `USER` 전용 API 호출 가능 |
| 7 | **`authorizeUser()` 메서드 의미 혼동** | `User.java` | 🟢 낮음 | 회원가입 완료와 닉네임 수정 모두에서 동일한 `authorizeUser(nickname)` 호출. 닉네임 수정 시에도 `role = USER`를 재설정하는 부수 효과. 기능별 메서드 분리 필요 (`updateNickname()` 별도) |
| 8 | **`ApiResponse`에 불필요한 import** | `ApiResponse.java` | 🟢 낮음 | `CounselResDTO` import가 존재하나 사용되지 않음. 데드 코드 |
| 9 | **`SocialType.from()` 예외 처리** | `SocialType.java` | 🟢 낮음 | `IllegalArgumentException`을 throw하지만 `GeneralException`이 아님. `ExceptionAdvice`에서 500으로 처리됨 |

### ② 에러 핸들링 확인 — 코드 레벨 분석 (15점)

| 시나리오 | 코드 대응 여부 | 상세 |
|---|---|---|
| 미인증 사용자 접근 | ✅ 처리됨 | `JwtAuthenticationFilter` + `SecurityConfig.anyRequest().authenticated()` |
| 존재하지 않는 사용자 | ✅ 처리됨 | 모든 메서드에서 `findById().orElseThrow()` |
| 닉네임 빈값 | ✅ 처리됨 | `checkNicknameDuplication()`에서 null/trim 검사 |
| 닉네임 형식 위반 | ✅ 처리됨 | 정규식 `^[가-힣a-zA-Z0-9]{3,10}$` 검증 |
| 닉네임 중복 | ✅ 처리됨 | `existsByNickname()` + `_NICKNAME_CONFLICT` |
| 지원하지 않는 소셜 로그인 | ✅ 처리됨 | `_INVALID_PROVIDER` |
| 소셜 서버 통신 실패 | ✅ 처리됨 | try-catch → `_SOCIAL_LOGIN_FAILED` |
| 탈퇴 후 7일 내 재가입 | ✅ 처리됨 | `_WITHDRAWAL_RESTRICTION` |
| 유효하지 않은 Refresh Token | ⚠️ 부분 처리 | `IllegalArgumentException` throw → 500 응답 (적절한 에러코드 미사용) |
| 타인의 userId로 탈퇴 요청 | ❌ 미처리 | RequestBody userId 사용, 소유권 검증 없음 |
| GUEST 권한 사용자 API 접근 | ❌ 미처리 | Filter에서 Role 미검증, GUEST도 모든 API 접근 가능 |
| 상담사 설정 미존재 시 | ✅ 처리됨 | `COUNSELOR_NOT_FOUND` |
| 미션 Draft 미존재 시 | ✅ 처리됨 | `_DRAFT_NOT_FOUND` |

### ③ 참고 서류 (5점)

> 📌 이 항목은 Notion, 영상, README 등 외부 문서로 평가되므로 정적 분석 범위 밖입니다.

---

## 🔧 발견된 주요 이슈 및 개선 권고

### 🔴 즉시 수정 권장 (감점 직결)

| # | 이슈 | 수정 방법 |
|---|------|-----------|
| 1 | `withdraw()` 보안 취약점 | `WithdrawRequest` 제거, `@AuthenticationPrincipal Long userId`로 변경. `DELETE /api/v1/auth/withdraw`에서 `@RequestBody` 제거 |
| 2 | `reissue()` `IllegalArgumentException` | `throw new GeneralException(ErrorStatus._UNAUTHORIZED)` 또는 신규 에러코드 `_INVALID_REFRESH_TOKEN` 추가 |
| 3 | `DevAuthController` 제거 | 프로덕션 코드에서 삭제, `SecurityConfig`의 `/api/v1/dev/**` permitAll 제거 |
| 4 | `JwtAuthenticationFilter` Role 권한 적용 | JWT에서 role 추출 → `SimpleGrantedAuthority` 생성 → `SecurityConfig`에서 `.requestMatchers("/api/v1/users/signup").hasRole("GUEST")`, 나머지 `.hasRole("USER")` 설정 |

### 🟡 개선 권장

| # | 이슈 | 수정 방법 |
|---|------|-----------|
| 5 | `UserService` 비대화 | `UserProfileService`, `UserSettingsService`, `MissionHistoryService` 분리. 또는 Command/Query 분리 패턴 적용 |
| 6 | `updateCounselorSettings()` 엔티티 재생성 | `CounselorSettings`에 `update()` 메서드 추가, Dirty Checking 활용 |
| 7 | `authorizeUser()` 이중 용도 | `authorizeUser(nickname)` (회원가입 전용) + `updateNickname(nickname)` (수정 전용) 분리 |
| 8 | `@Valid` 전면 적용 | 모든 `@RequestBody`에 `@Valid` 추가, DTO 필드에 `@NotBlank`, `@Pattern` 등 적용 |
| 9 | URL 트레일링 슬래시 제거 | `@RequestMapping("/api/v1/users/")` → `@RequestMapping("/api/v1/users")` |
| 10 | `NicknameCheckResponse` 분리 | Controller 내부 record → `dto` 패키지로 이동 |
| 11 | `ErrorStatus` 네이밍 통일 | 모든 에러코드에 `_` 접두사 통일 또는 제거 (`COUNSELOR_NOT_FOUND` → `_COUNSELOR_NOT_FOUND`) |

---

## 🔎 Global 인프라 분석에서 발견된 프로젝트 전체 교차 인사이트

> Global 폴더의 전체 소스 코드 분석을 기반으로 **6개 도메인 전체에 영향을 미치는** 아키텍처 레벨 인사이트

### 🔴 심각도 높음 — 전체 도메인 영향

| # | 항목 | 영향 범위 | 상세 |
|---|------|-----------|------|
| 1 | **`JwtAuthenticationFilter`에서 Role 미검증** | 전 도메인 | JWT claim에 `role`이 포함되지만, Filter에서 `new ArrayList<>()`(빈 권한)로 Authentication 생성. `GUEST` 사용자도 모든 도메인 API 호출 가능. `SecurityConfig`에서 `anyRequest().authenticated()`만 있고 Role 기반 접근 제어 없음 |
| 2 | **`DevAuthController` 보안 위험** | 전 도메인 | `GET /api/v1/dev/login?userId=1`로 임의 사용자 토큰 발급 가능. 이 토큰으로 모든 도메인 API 접근 가능 |
| 3 | **`ExceptionAdvice` 불충분한 에러 처리** | 전 도메인 | `GeneralException`이 아닌 표준 예외(`IllegalArgumentException` 등)를 throw하면 `exception()` 핸들러에 잡혀 `500 INTERNAL_SERVER_ERROR` + 에러 메시지 노출. 실제로 `AuthService.reissue()`, `SocialType.from()`에서 이 문제 발생 |

### 🟡 아키텍처 불일관성 — 전체 도메인 영향

| # | 항목 | 현재 상태 | 개선 방향 |
|---|------|-----------|-----------|
| 4 | **에러코드 아키텍처 이원화** | Counsel(`CounselErrorCode`), Diagnose(`DiagnoseErrorCode`)만 도메인 전용 보유. Home, Report, TodayMission, User는 글로벌 `ErrorStatus`에 혼재 (현재 **26개** 에러코드 글로벌에 집중) | 전 도메인 전용 `ErrorCode` enum 생성 또는, 글로벌 ErrorStatus를 카테고리별로 정리 |
| 5 | **`ErrorStatus` 네이밍 컨벤션 불일관** | `_MEMBER_NOT_FOUND` (접두사 有) vs `COUNSELOR_NOT_FOUND` (접두사 無) vs `TEMP_EXCEPTION` (접두사 無) | 통일 컨벤션 적용 |
| 6 | **Service 구조 3패턴 혼재** | ①Command/Query 분리(Counsel, Diagnose) ②기능별 분리(Home, Report) ③단일 Service(User ~357줄, TodayMission ~200줄) | 프로젝트 전체 Service 구조 패턴 통일 |
| 7 | **AI 클라이언트 이원화** | Counsel: Spring AI `ChatClient` (AiConfig) / Report: `RestTemplate` 직접 OpenAI 호출. `AiConfig`에서 `ChatClient`와 `RestTemplate` 모두 Bean 등록 | `ChatClient`로 통일 |
| 8 | **`@Valid` 전 도메인 미적용** | 모든 6개 도메인에서 `@Valid` 미사용. Bean Validation 미활용 | `@Valid` + DTO에 `@NotBlank`, `@Size`, `@Pattern` 일괄 적용 |
| 9 | **`@Transactional` 적용 불일관** | User: 클래스 레벨 `@Transactional(readOnly=true)` ✅ / Report: 클래스 레벨 `@Transactional` ✅ / TodayMission: 메서드별 선택 적용 / Home, Counsel: 일부 누락 | 조회 메서드에 `@Transactional(readOnly=true)`, 수정 메서드에 `@Transactional` 일괄 적용 |
| 10 | **DTO 스타일 3종 혼재** | ① Java `record` (User: `UserProfileResponse`, `UserSettingsResponse`) ② `@Getter` class (User: `UserSignUpRequest`, 대부분 도메인) ③ Inner class (Report: `ReportsResponse`, User: `MissionHistoryResponse`) | record 패턴으로 통일 권장 |

---

## 📊 도메인 간 비교 (전체 6개 도메인 최종)

| 평가 항목 | Counsel | Diagnose | Home | Report | TodayMission | User | 비고 |
|---|---|---|---|---|---|---|---|
| RESTful 원칙 준수 | 3.5 / 5 | **5.0 / 5** | 3.5 / 5 | 3.5 / 5 | 2.5 / 5 | 3.5 / 5 | Diagnose 최고, Mission 최저 |
| 공통 응답 포맷 사용 | 4.0 / 5 | **5.0 / 5** | 4.5 / 5 | **5.0 / 5** | **5.0 / 5** | 4.0 / 5 | 3개 도메인 만점 |
| 파라미터 처리 일관성 | 3.0 / 5 | 3.5 / 5 | 3.0 / 5 | **4.5 / 5** | 3.0 / 5 | 3.5 / 5 | Report 가장 일관적 |
| 에러 핸들링 | **4.0 / 5** | **4.0 / 5** | 3.0 / 5 | 3.0 / 5 | 3.0 / 5 | 3.5 / 5 | 전용 에러코드 보유 도메인이 높음 |
| **합계** | **14.5 / 20** | **17.5 / 20** | **14.0 / 20** | **16.0 / 20** | **13.5 / 20** | **14.5 / 20** | |

### 최종 순위

| 순위 | 도메인 | 점수 | 주요 강점 | 주요 약점 |
|---|---|---|---|---|
| 🥇 | **Diagnose** | **17.5 / 20** | RESTful 만점, 전용 에러코드, DTO 변환 일관 | `@Transactional` 일부 누락 |
| 🥈 | **Report** | **16.0 / 20** | 응답 포맷 만점, 파라미터 처리 최고 | AI 에러 무시, RestTemplate 직접 사용 |
| 🥉 | **Counsel** | **14.5 / 20** | 풍부한 에러 처리, Command/Query 분리 | `saveSettings()` ResponseEntity 반환 |
| 4위 | **User** | **14.5 / 20** | 11개 엔드포인트 모두 ApiResponse, record DTO | withdraw 보안, DevAuth 잔존, IllegalArgumentException |
| 5위 | **Home** | **14.0 / 20** | 단순하고 명확한 구조 | @RequestParam으로 PATCH, 에러코드 없음 |
| 6위 | **TodayMission** | **13.5 / 20** | 에러 시나리오 풍부한 비즈니스 로직 | 동사형 URL 다수, 소유권 미검증, Service 비대 |

---

### 📊 프로젝트 전체 평균: **15.0 / 20점**

---

## 📝 비고

- 이 보고서는 User 도메인 + Global 인프라 전체의 **소스 코드 정적 분석**만을 기반으로 작성되었습니다.
- User 도메인은 Auth 관련 보안 이슈(withdraw 취약점, DevAuth 잔존, Role 미검증)가 주요 감점 원인입니다.
- Global 인프라 분석에서 발견된 **JwtAuthenticationFilter Role 미검증**, **DevAuthController 잔존**, **ErrorStatus 네이밍 불일관** 등은 전 도메인에 영향을 미치는 프로젝트 레벨 이슈입니다.
- 1-2 항목의 **정상 동작 여부**(20점)와 **에러 핸들링 확인**(15점)은 실제 Swagger 테스트를 통해 최종 평가되어야 합니다.
