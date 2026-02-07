# 📋 Home 도메인 — API 평가 기준 정적 분석 보고서

> **분석 대상**: `com.symteo.domain.home`  
> **분석 일시**: 2025-02-07  
> **분석 범위**: 평가 기준 1-1 (API 설계 완성도) + 1-2 (API 구현 완성도 — 정적 분석 가능 범위)

---

## 1-1. API 설계 완성도 (총 20점)

---

### ① RESTful 원칙 준수 (5점)

#### 엔드포인트 목록

| HTTP 메서드 | URL 패턴 | 기능 | RESTful 적합성 |
|---|---|---|---|
| `GET` | `/api/v1/home/` | 홈 화면 전체 데이터 조회 | ⚠️ 아래 상세 설명 |
| `PATCH` | `/api/v1/home/today-weather` | 감정 날씨 수정/생성 | ⚠️ 아래 상세 설명 |

#### 상세 분석

**✅ 양호 항목**
- API 버전 관리(`v1`)가 URL에 포함됨
- `GET`으로 조회, `PATCH`로 수정이라는 HTTP 메서드 선택 자체는 적절

**⚠️ 개선 필요 항목**

| # | 항목 | 현재 상태 | 권장 사항 | 감점 요인 |
|---|------|-----------|-----------|-----------|
| 1 | **URL 끝 슬래시** | `GET /api/v1/home/` | URL 끝에 `/`가 포함됨. `/api/v1/home`으로 통일 권장. 다른 도메인(`/api/v1/counsels`, `/api/v1/diagnoses`)과 **불일관** | 경미 |
| 2 | **리소스 단수형** | `/api/v1/home` | `home`은 단수형. 다른 도메인들이 복수형(`counsels`, `diagnoses`)을 사용하므로 통일성 부족. 단, 홈 화면은 싱글턴 리소스이므로 단수형이 허용 가능 | 경미 |
| 3 | **PATCH의 생성+수정 혼용** | `PATCH /api/v1/home/today-weather` | `PATCH`는 기존 리소스의 **부분 수정**을 의미. 생성(`CREATE`)까지 포함하는 "upsert" 동작은 `PUT`이 더 적합 (PUT은 있으면 대체, 없으면 생성의 멱등 연산) | 중간 |
| 4 | **하위 리소스 분리 부족** | `/api/v1/home/today-weather` | 감정 날씨는 독립된 리소스. `/api/v1/today-emotions` 또는 `/api/v1/home/emotions`처럼 명사형 리소스로 분리하는 것이 RESTful | 경미 |
| 5 | **엔드포인트 수 부족** | 2개만 존재 | 홈 도메인에 조회 1개 + 수정 1개만 있음. 기능 범위가 좁아 API 설계 평가의 다양성이 부족할 수 있음 | — (도메인 특성상 불가피) |

#### 예상 점수: **3.5 / 5점**

---

### ② 공통 응답 포맷 사용 (5점)

#### 응답 구조 분석

| 엔드포인트 | 반환 타입 | 공통 포맷 준수 |
|---|---|---|
| `GET /home/` | `ApiResponse<HomeResponseDto>` | ✅ |
| `PATCH /home/today-weather` | `ApiResponse<Integer>` | ✅ |

**✅ 양호 항목**
- 모든 엔드포인트가 `ApiResponse<T>`를 일관되게 반환
- 실패 응답도 `ExceptionAdvice` → `ApiResponse.onFailure()` 경로로 통일 처리됨

**⚠️ 개선 필요 항목**

| # | 항목 | 상세 |
|---|------|------|
| 1 | **`ApiResponse<Integer>` 반환** | `PATCH /today-weather`가 `Integer` 원시 래퍼를 직접 반환. 다른 도메인들은 전용 응답 DTO(`CreateDTO`, `ChatMessage` 등)를 사용. 확장성·가독성을 위해 `HomeResponseDto.WeatherResult(Integer weather)` 같은 전용 DTO 권장 |

#### 예상 점수: **4.5 / 5점**

---

### ③ 파라미터 처리 일관성 (5점)

#### 파라미터 바인딩 분석

| 엔드포인트 | 파라미터 방식 | 적합성 |
|---|---|---|
| `GET /home/` | `@AuthenticationPrincipal Long userId` | ✅ |
| `PATCH /home/today-weather` | `@AuthenticationPrincipal Long userId` + `@RequestParam Integer weather` | ⚠️ 아래 설명 |

**✅ 양호 항목**
- 모든 엔드포인트에서 `@AuthenticationPrincipal Long userId`를 일관되게 사용
- 인증 토큰에서 사용자 ID를 추출하는 패턴 유지

**⚠️ 개선 필요 항목**

| # | 항목 | 상세 |
|---|------|------|
| 1 | **`@RequestParam`으로 수정 데이터 수신** | `PATCH /today-weather?weather=3` 형태. 리소스의 상태를 변경하는 요청은 `@RequestBody`로 JSON을 받는 것이 일반적. 다른 도메인(`counsels`, `diagnoses`)은 모두 `@RequestBody` DTO를 사용하므로 **불일관** |
| 2 | **파라미터 검증 부재** | `weather` 값에 대한 범위 검증이 없음. 1~4 범위만 유효한데, `@Min(1) @Max(4)` 같은 Bean Validation이 미적용. 0, -1, 999 등 비정상 값이 그대로 저장됨 |
| 3 | **`@Valid` 미적용** | `@RequestParam`에도 Controller 레벨에서 `@Validated`를 클래스에 추가하면 `@Min/@Max` 등의 검증이 가능하나, 미적용 상태 |

#### 예상 점수: **3.0 / 5점**

---

### ④ 에러 핸들링 (5점)

#### 에러 코드 체계

**도메인 전용 에러코드**: ❌ **없음** — `DiagnoseErrorCode`, `CounselErrorCode`처럼 도메인 전용 에러코드 enum이 존재하지 않음

**글로벌 에러코드 (`ErrorStatus`) 활용:**

| 에러 코드 | 사용 위치 | HTTP 상태 |
|---|---|---|
| `_MEMBER_NOT_FOUND` | `HomeService`, `TodayEmotionWeatherService`, `TodayLineService` | `404 NOT_FOUND` |
| `_TODAY_LINE_NOT_FOUND` | `TodayLineService` | `404 NOT_FOUND` |

**✅ 양호 항목**
- `userRepository.findById().orElseThrow(GeneralException)`으로 사용자 미존재 시 예외 처리
- `TodayLineService`에서 문구 0건일 때 `_TODAY_LINE_NOT_FOUND` 예외 처리
- `TodayEmotionWeatherService.getTodayEmotion()`에서 미선택 시 `null` 반환 (예외가 아닌 정상 응답으로 처리 — 적절)
- `@Transactional(readOnly = true)` 조회 메서드에 적용, `@Transactional` 수정 메서드에 적용

**⚠️ 개선 필요 항목**

| # | 항목 | 상세 |
|---|------|------|
| 1 | **도메인 전용 에러코드 부재** | Home 도메인에 `HomeErrorCode` enum이 없음. 모든 예외가 글로벌 `ErrorStatus`에 의존. 다른 도메인들은 전용 에러코드를 가지고 있으므로 **아키텍처 불일관** |
| 2 | **`weather` 입력값 검증 부재** | 1~4 외의 값(0, -1, 100 등)이 들어와도 예외 없이 DB에 저장됨. 비즈니스 룰 위반 데이터가 축적될 수 있음 |
| 3 | **`TodayLineService` 인덱스 에러 가능** | `page.getContent().get(0)` 호출 시, `totalCount > 0`이더라도 페이징 인덱스가 범위를 벗어나면 `IndexOutOfBoundsException` 발생 가능 (예: 데이터 삭제 타이밍) |
| 4 | **`TodayLines.createdAt` 수동 설정** | `this.createdAt = LocalDateTime.now()`로 직접 설정. JPA 표준인 `@CreationTimestamp` 또는 `@PrePersist`를 사용하는 것이 테스트·일관성 측면에서 권장됨 (`TodayEmotions`도 동일) |

#### 예상 점수: **3.0 / 5점**

---

### 📊 1-1 종합 점수 (정적 분석 기반 예상)

| 평가 항목 | 배점 | 예상 점수 | 비고 |
|---|---|---|---|
| RESTful 원칙 준수 | 5점 | **3.5점** | PATCH upsert, URL 끝 슬래시, 리소스 네이밍 |
| 공통 응답 포맷 사용 | 5점 | **4.5점** | 전 엔드포인트 ApiResponse 통일, Integer 직접 반환 |
| 파라미터 처리 일관성 | 5점 | **3.0점** | @RequestParam 사용 비일관, 입력 검증 부재 |
| 에러 핸들링 | 5점 | **3.0점** | 도메인 에러코드 부재, 입력값 검증 없음 |
| **합계** | **20점** | **14.0점** | |

---

## 1-2. API 구현 완성도 — 정적 분석 가능 범위

> ⚠️ 1-2 항목은 본래 **실제 서버 실행(Swagger 테스트)** 을 통해 평가하는 항목입니다.  
> 아래는 코드만으로 추론 가능한 부분을 정리한 것이며, 실제 점수와 차이가 있을 수 있습니다.

---

### ① 정상 동작 여부 — 코드 레벨 위험 요소 (20점)

| # | 위험 요소 | 위치 | 심각도 | 상세 |
|---|-----------|------|--------|------|
| 1 | **`page.getContent().get(0)` NPE/IOOB** | `TodayLineService.getTodayLine()` | 🔴 높음 | `totalCount`와 실제 `findAll()` 결과 사이에 타이밍 차이 발생 시, `Page`가 비어있어 `IndexOutOfBoundsException` 발생 가능. `page.hasContent()` 검사 필요 |
| 2 | **`weather` 범위 미검증** | `TodayEmotionWeatherService` | 🟡 중간 | 1~4 범위 밖의 값(0, -1, 999)이 DB에 그대로 저장됨. 클라이언트에 잘못된 감정 데이터가 반환될 수 있음 |
| 3 | **`createdAt` 수동 설정** | `TodayEmotions`, `TodayLines` | 🟡 중간 | `LocalDateTime.now()`로 직접 설정 → 테스트 시 시간 제어 불가, 서버 시간대 의존성 발생 |
| 4 | **Converter 부재** | 도메인 전체 | 🟢 낮음 | 다른 도메인(`counsel`, `diagnose`)은 Converter 클래스를 통해 Entity↔DTO 변환. Home은 Service에서 직접 `new HomeResponseDto()`를 호출. 소규모 도메인이므로 현재는 허용 가능하나, 확장 시 분리 권장 |

### ② 에러 핸들링 확인 — 코드 레벨 분석 (15점)

| 시나리오 | 코드 대응 여부 | 상세 |
|---|---|---|
| 미인증 요청 | ✅ 처리됨 | 모든 엔드포인트에 `@AuthenticationPrincipal` 적용 |
| 존재하지 않는 userId | ✅ 처리됨 | 3개 서비스 모두 `userRepository.findById().orElseThrow()` |
| 오늘의 한 줄 데이터 0건 | ✅ 처리됨 | `totalCount == 0` 검사 → `_TODAY_LINE_NOT_FOUND` 예외 |
| 감정 미선택 상태 조회 | ✅ 처리됨 | `Optional.map().orElse(null)` — null 반환으로 정상 처리 |
| 비정상 weather 값 (0, -1, 999) | ❌ 미처리 | 검증 없이 DB 저장 |
| weather 파라미터 누락 | ✅ 처리됨 | `@RequestParam`은 기본적으로 required=true → Spring이 `MissingServletRequestParameterException` 발생 → `ExceptionAdvice`에서 처리 |
| 동시 요청 (race condition) | ⚠️ 부분 처리 | `updateOrCreateEmotion()`에서 조회-생성 사이에 동시 요청 시 중복 레코드 생성 가능. DB unique 제약 조건 또는 비관적 락 권장 |

### ③ 참고 서류 (5점)

> 📌 이 항목은 Notion, 영상, README 등 외부 문서로 평가되므로 정적 분석 범위 밖입니다.

---

## 🔧 발견된 주요 이슈 및 개선 권고

### 🔴 즉시 수정 권장 (감점 직결)

| # | 이슈 | 수정 방법 |
|---|------|-----------|
| 1 | `@RequestParam Integer weather`로 수정 데이터 수신 | `@RequestBody` + 전용 DTO(`HomeReqDto.UpdateWeather`)로 변경. 프로젝트 전체 일관성 유지 |
| 2 | `weather` 값 검증 부재 | DTO에 `@Min(1) @Max(4)` 추가 + Controller에 `@Valid` 적용. 또는 서비스 계층에서 범위 검증 후 커스텀 예외 발생 |
| 3 | URL 끝 슬래시 제거 | `@GetMapping("/")` → `@GetMapping("")` 변경 |
| 4 | 도메인 전용 에러코드 생성 | `HomeErrorCode` enum 생성, `HomeException` 클래스 생성. `_TODAY_LINE_NOT_FOUND` 등을 도메인 에러코드로 이동 |

### 🟡 개선 권장

| # | 이슈 | 수정 방법 |
|---|------|-----------|
| 5 | PATCH upsert → PUT 변경 | `PATCH /today-weather` → `PUT /today-weather` (멱등 upsert에 적합) |
| 6 | `page.getContent().get(0)` 안전성 | `page.hasContent()` 검사 추가 또는 `page.getContent().stream().findFirst().orElseThrow()` |
| 7 | `createdAt` JPA 표준 사용 | `this.createdAt = LocalDateTime.now()` → `@CreationTimestamp` 또는 `@PrePersist` 적용 |
| 8 | `ApiResponse<Integer>` → 전용 DTO | `HomeResponseDto.WeatherResult` 등 응답 DTO 생성하여 확장성 확보 |

---

## 📊 도메인 간 비교 (누적)

| 평가 항목 | Counsel | Diagnose | Home | 비고 |
|---|---|---|---|---|
| RESTful 원칙 준수 | 3.5 / 5 | **5.0 / 5** | 3.5 / 5 | diagnose가 가장 깔끔 |
| 공통 응답 포맷 사용 | 4.0 / 5 | **5.0 / 5** | 4.5 / 5 | diagnose 만점, home은 Integer 직접 반환 |
| 파라미터 처리 일관성 | 3.0 / 5 | 3.5 / 5 | 3.0 / 5 | 전 도메인 `@Valid` 미적용 |
| 에러 핸들링 | **4.0 / 5** | **4.0 / 5** | 3.0 / 5 | home은 도메인 에러코드 부재 |
| **합계** | **14.5 / 20** | **17.5 / 20** | **14.0 / 20** | |

---

## � 교차 도메인 분석에서 발견된 추가 사항

> `application.yaml` 및 타 도메인 분석을 통해 발견된 home 도메인 관련 추가 인사이트

| # | 항목 | 상세 |
|---|------|------|
| 1 | **`TodayEmotions.createdAt` vs Entity 표준** | Report 도메인의 `Reports` 엔티티는 `@PrePersist`를 사용하여 `createdAt`을 설정. Home 도메인의 `TodayEmotions`와 `TodayLines`는 Builder에서 `LocalDateTime.now()`를 직접 호출. 프로젝트 내에서도 JPA 생명주기 관리 방식이 **불일관** (`@CreationTimestamp` vs `@PrePersist` vs 수동 설정 — 3가지 방식 혼재) |
| 2 | **도메인 에러코드 비교** | Counsel(`CounselErrorCode`), Diagnose(`DiagnoseErrorCode`)는 전용 에러코드를 가지나, Home과 Report는 글로벌 `ErrorStatus`에만 의존. 4개 도메인 중 2개만 전용 에러코드를 가져 **절반이 미충족** |
| 3 | **DB 마이그레이션 확인** | `application.yaml`에서 Flyway 활성화 확인. `V13__update_today_lines_table.sql` 마이그레이션 파일이 존재하여 DB 스키마 관리는 적절 |

> 🆕 **Global 인프라 분석 (전체 도메인 공통 영향)**

| # | 항목 | Home 도메인 영향 |
|---|------|---|
| 4 | **`JwtAuthenticationFilter`에서 Role 미검증** | `GUEST` 사용자도 홈 화면 API 호출 가능. 감정 날씨 기록 등은 회원 전용 기능이어야 할 가능성 |
| 5 | **`DevAuthController` 프로덕션 잔존** | 임의 토큰으로 홈 API 접근 가능 — 전 도메인 공통 보안 리스크 |
| 6 | **`@Valid` 전 도메인 미적용** | home 도메인의 `@RequestParam Integer weather`에는 `@Min(1) @Max(4)` 같은 범위 검증이 필요하나 미적용 |
| 7 | **Service 구조 패턴 비교** | Home은 기능별 Service 분리 (`HomeService`, `TodayEmotionWeatherService`, `TodayLineService`). 이는 Counsel/Diagnose의 Command/Query 분리, User/TodayMission의 단일 Service와 모두 다른 3번째 패턴. 프로젝트 전체 3종 구조 혼재 |

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
- Home 도메인은 엔드포인트가 2개로 규모가 작아, 평가 항목 충족 폭이 제한적입니다.
- 1-2 항목의 **정상 동작 여부**(20점)와 **에러 핸들링 확인**(15점)은 실제 Swagger 테스트를 통해 최종 평가되어야 합니다.
- 예상 점수는 평가자의 기준에 따라 달라질 수 있으며, 참고용으로만 활용하시기 바랍니다.
