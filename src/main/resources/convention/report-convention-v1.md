# 📋 Report 도메인 — API 평가 기준 정적 분석 보고서

> **분석 대상**: `com.symteo.domain.report`  
> **분석 일시**: 2025-02-07  
> **분석 범위**: 평가 기준 1-1 (API 설계 완성도) + 1-2 (API 구현 완성도 — 정적 분석 가능 범위)

---

## 1-1. API 설계 완성도 (총 20점)

---

### ① RESTful 원칙 준수 (5점)

#### 엔드포인트 목록

| HTTP 메서드 | URL 패턴 | 기능 | RESTful 적합성 |
|---|---|---|---|
| `POST` | `/api/v1/reports/depression-anxiety/{diagnoseId}` | 우울/불안 리포트 생성 | ✅ |
| `GET` | `/api/v1/reports/depression-anxiety/{reportId}` | 우울/불안 리포트 조회 | ⚠️ 아래 설명 |
| `POST` | `/api/v1/reports/stress-burnout/{diagnoseId}` | 스트레스/번아웃 리포트 생성 | ✅ |
| `GET` | `/api/v1/reports/stress-burnout/{reportId}` | 스트레스/번아웃 리포트 조회 | ⚠️ 아래 설명 |
| `POST` | `/api/v1/reports/attachment/{diagnoseId}` | 성향 리포트 생성 | ✅ |
| `GET` | `/api/v1/reports/attachment/{reportId}` | 성향 리포트 조회 | ⚠️ 아래 설명 |

#### 상세 분석

**✅ 양호 항목**
- 기본 URL `@RequestMapping("/api/v1/reports")`가 리소스 중심(명사, 복수형)으로 설계됨
- API 버전 관리(`v1`)가 URL에 포함됨
- `POST`(생성)와 `GET`(조회)의 HTTP 메서드 선택이 적절
- 리포트 유형을 URL 경로로 구분 (`/depression-anxiety`, `/stress-burnout`, `/attachment`)

**⚠️ 개선 필요 항목**

| # | 항목 | 현재 상태 | 권장 사항 | 감점 요인 |
|---|------|-----------|-----------|-----------|
| 1 | **동일 PathVariable 이름에 다른 의미** | `POST /{diagnoseId}` → 진단 ID / `GET /{reportId}` → 리포트 ID | 같은 URL 패턴 위치에 다른 ID가 사용됨. REST 관점에서 혼란 가능. 생성은 `/api/v1/reports/depression-anxiety?diagnoseId=1` (쿼리) 또는 `/api/v1/diagnoses/{diagnoseId}/reports/depression-anxiety` (계층) 형태가 더 명확 | 중간 |
| 2 | **Controller에서 직접 Repository 호출** | `ReportsController`에서 `diagnoseRepository.findById()` 직접 호출 | Controller → Service → Repository 계층 구조 위반. 진단 조회 로직은 Service 내부에서 처리해야 함 | 중간 |
| 3 | **리포트 삭제 API 부재** | 삭제 엔드포인트 없음 | CRUD 중 Delete가 빠져 있음. 비즈니스 요구에 따라 불필요할 수 있으나, 평가 시 완성도 측면에서 감점 가능 | 경미 |

#### 예상 점수: **3.5 / 5점**

---

### ② 공통 응답 포맷 사용 (5점)

#### 응답 구조 분석

| 엔드포인트 | 반환 타입 | 공통 포맷 준수 |
|---|---|---|
| `POST /reports/depression-anxiety/{diagnoseId}` | `ApiResponse<ReportsResponse.CreateReportResult>` | ✅ |
| `GET /reports/depression-anxiety/{reportId}` | `ApiResponse<ReportsResponse.DepressionAnxietyReportDetail>` | ✅ |
| `POST /reports/stress-burnout/{diagnoseId}` | `ApiResponse<ReportsResponse.CreateReportResult>` | ✅ |
| `GET /reports/stress-burnout/{reportId}` | `ApiResponse<ReportsResponse.IntegratedReportDetail>` | ✅ |
| `POST /reports/attachment/{diagnoseId}` | `ApiResponse<ReportsResponse.CreateReportResult>` | ✅ |
| `GET /reports/attachment/{reportId}` | `ApiResponse<ReportsResponse.AttachmentReportDetail>` | ✅ |

**✅ 양호 항목**
- 모든 6개 엔드포인트가 `ApiResponse<T>`를 일관되게 반환
- 생성 응답에 `CreateReportResult` 공통 DTO 재사용 — 통일성 높음
- 조회 응답에 리포트 유형별 전용 상세 DTO 사용 — 명확한 데이터 구조
- 실패 응답도 `ExceptionAdvice` → `ApiResponse.onFailure()` 경로 통일

#### 예상 점수: **5.0 / 5점**

---

### ③ 파라미터 처리 일관성 (5점)

#### 파라미터 바인딩 분석

| 엔드포인트 | 파라미터 방식 | 적합성 |
|---|---|---|
| `POST /depression-anxiety/{diagnoseId}` | `@PathVariable Long diagnoseId` + `@AuthenticationPrincipal Long userId` | ✅ |
| `GET /depression-anxiety/{reportId}` | `@PathVariable Long reportId` + `@AuthenticationPrincipal Long userId` | ✅ |
| `POST /stress-burnout/{diagnoseId}` | `@PathVariable Long diagnoseId` + `@AuthenticationPrincipal Long userId` | ✅ |
| `GET /stress-burnout/{reportId}` | `@PathVariable Long reportId` + `@AuthenticationPrincipal Long userId` | ✅ |
| `POST /attachment/{diagnoseId}` | `@PathVariable Long diagnoseId` + `@AuthenticationPrincipal Long userId` | ✅ |
| `GET /attachment/{reportId}` | `@PathVariable Long reportId` + `@AuthenticationPrincipal Long userId` | ✅ |

**✅ 양호 항목**
- 모든 엔드포인트에서 `@AuthenticationPrincipal Long userId`를 일관되게 사용
- `@PathVariable`로 리소스 식별 — POST에 `@RequestBody`가 불필요한 구조 (진단 데이터를 DB에서 조회하므로 ID만 전달)
- 6개 엔드포인트의 파라미터 패턴이 완전 통일

**⚠️ 개선 필요 항목**

| # | 항목 | 상세 |
|---|------|------|
| 1 | **Controller에서 diagnose 검증** | 3개 POST 엔드포인트 모두에서 `diagnoseRepository.findById().orElseThrow()`를 Controller가 직접 수행. 이 검증은 Service 계층의 책임. 동일 로직 3번 중복 |

#### 예상 점수: **4.5 / 5점**

---

### ④ 에러 핸들링 (5점)

#### 에러 코드 체계

**도메인 전용 에러코드**: ❌ **없음** — `ReportErrorCode`가 존재하지 않음

**글로벌 에러코드 (`ErrorStatus`) 활용:**

| 에러 코드 | 사용 위치 | HTTP 상태 |
|---|---|---|
| `_MEMBER_NOT_FOUND` | 모든 Service `analyzeAndSave()` | `404 NOT_FOUND` |
| `_DIAGNOSE_NOT_FOUND` | `ReportsController` 3개 POST | `404 NOT_FOUND` |
| `_REPORT_NOT_FOUND` | 모든 Service `getReportDetail()` | `404 NOT_FOUND` |
| `_UNAUTHORIZED` | `getReportDetail()` 권한 검증 | `401 UNAUTHORIZED` |

**✅ 양호 항목**
- 리포트 조회 시 `userId` 소유권 검증 구현 (`report.getUser().getId().equals(userId)`)
- 존재하지 않는 진단 ID / 리포트 ID에 대한 예외 처리
- 중복 리포트 생성 시 예외 대신 기존 리포트 반환 — 멱등성 확보
- `@Transactional` 클래스 레벨 적용, 조회에 `@Transactional(readOnly = true)` 사용

**⚠️ 개선 필요 항목**

| # | 항목 | 상세 |
|---|------|------|
| 1 | **도메인 전용 에러코드 부재** | `ReportErrorCode` enum이 없음. counsel, diagnose는 전용 에러코드를 가지고 있으므로 아키텍처 불일관 |
| 2 | **`_UNAUTHORIZED` 사용 부적절** | 리포트 소유권 검증 실패 시 `_UNAUTHORIZED(401)` 반환. 인증은 이미 완료된 상태이므로 `_FORBIDDEN(403)`이 올바른 HTTP 상태 코드 |
| 3 | **AI API 실패 시 에러 미전파** | `AiModelServiceImpl.callAiApi()`에서 예외 발생 시 에러 문자열(`"AI 분석 도중 오류가 발생했습니다."`)을 반환. 예외를 삼켜서 리포트에 에러 메시지가 AI 분석 결과로 저장됨. `GeneralException`으로 전파해야 함 |
| 4 | **`AttachmentReportsService.getReportDetail()` 소유권 검증 누락** | `DepressionAnxietyReportsService`와 `StressReportsService`는 `userId` 검증을 하지만, `AttachmentReportsService`는 검증 없이 리포트를 반환 |
| 5 | **`StressReportsService.getReportDetail()` NPE 위험** | `st`, `bu`가 `null`일 수 있는데(`orElse(null)`), 이후 `st.getStressScore()` 등을 직접 호출하여 `NullPointerException` 위험 |

#### 예상 점수: **3.0 / 5점**

---

### 📊 1-1 종합 점수 (정적 분석 기반 예상)

| 평가 항목 | 배점 | 예상 점수 | 비고 |
|---|---|---|---|
| RESTful 원칙 준수 | 5점 | **3.5점** | PathVariable 의미 혼동, Controller에서 Repository 직접 호출 |
| 공통 응답 포맷 사용 | 5점 | **5.0점** | 전 엔드포인트 ApiResponse 통일 |
| 파라미터 처리 일관성 | 5점 | **4.5점** | 인증/파라미터 패턴 통일, Controller 검증 중복만 감점 |
| 에러 핸들링 | 5점 | **3.0점** | 도메인 에러코드 없음, 401/403 혼용, AI 에러 삼킴, NPE 위험 |
| **합계** | **20점** | **16.0점** | |

---

## 1-2. API 구현 완성도 — 정적 분석 가능 범위

---

### ① 정상 동작 여부 — 코드 레벨 위험 요소 (20점)

| # | 위험 요소 | 위치 | 심각도 | 상세 |
|---|-----------|------|--------|------|
| 1 | **AI API 에러 삼킴** | `AiModelServiceImpl.callAiApi()` | 🔴 높음 | OpenAI 호출 실패 시 에러 메시지 문자열을 정상 결과처럼 반환 → 리포트에 `"AI 분석 도중 오류가 발생했습니다."`가 AI 분석 본문으로 저장됨. 사용자에게 에러 문자열이 리포트로 노출 |
| 2 | **`getReportDetail()` NPE** | `StressReportsService` | 🔴 높음 | `stressReportsRepository.findByReport(report).orElse(null)` 결과가 null이면, 다음 줄 `st.getStressScore()` 에서 NPE 발생 |
| 3 | **매직 넘버 대량 사용** | 전 Service | 🟡 중간 | `score >= 20`, `ratio >= 0.76`, `anxiety < 3.0`, `battery <= 25` 등 수십 개의 하드코딩된 기준값. 비즈니스 룰 변경 시 코드 전체를 뒤져야 함 |
| 4 | **DTO 파일 비대화** | `ReportsResponse.java` | 🟡 중간 | 하나의 파일에 13개의 inner static class 존재. 리포트 유형별로 파일 분리 권장 |
| 5 | **AI 분석 본문 구분자 `"||"`** | `DepressionAnxietyReportsService` | 🟡 중간 | 우울/불안 AI 결과를 `"||"`로 concat 후 저장 → 파싱 시 AI가 `"||"`를 포함한 텍스트를 생성하면 분리 실패 |
| 6 | **색상 코드 하드코딩** | `getColorByRatio()`, `getBatteryColor()`, `getScoreMetadata()` | 🟢 낮음 | `"#F4574F"`, `"#FFAC79"` 등 UI 색상이 서비스 로직에 하드코딩됨. 상수/enum으로 분리 권장 |
| 7 | **`System.err.println` 사용** | `AiModelServiceImpl` | 🟢 낮음 | `System.err.println` 대신 `@Slf4j` + `log.error()` 사용 권장 |

### ② 에러 핸들링 확인 — 코드 레벨 분석 (15점)

| 시나리오 | 코드 대응 여부 | 상세 |
|---|---|---|
| 존재하지 않는 diagnoseId | ✅ 처리됨 | `diagnoseRepository.findById().orElseThrow()` (Controller) |
| 존재하지 않는 reportId | ✅ 처리됨 | `reportsRepository.findById/findReportWithDetails().orElseThrow()` |
| 타인의 리포트 접근 | ⚠️ 부분 처리 | 우울/불안·스트레스는 검증 ✅, **성향 리포트는 미검증** ❌ |
| 미인증 요청 | ✅ 처리됨 | 모든 엔드포인트에 `@AuthenticationPrincipal` |
| 중복 리포트 생성 | ✅ 처리됨 | `findByDuplicateCheck()` → 기존 리포트 반환 (멱등성) |
| AI API 장애 | ❌ 미처리 | 에러를 삼키고 에러 문자열을 정상 데이터로 저장 |
| 잘못된 diagnoseType으로 리포트 생성 | ❌ 미처리 | 예: 우울/불안 진단 데이터로 스트레스 리포트 생성 시도 시 검증 없음 |

### ③ 참고 서류 (5점)

> 📌 이 항목은 Notion, 영상, README 등 외부 문서로 평가되므로 정적 분석 범위 밖입니다.

---

## 🔧 발견된 주요 이슈 및 개선 권고

### 🔴 즉시 수정 권장 (감점 직결)

| # | 이슈 | 수정 방법 |
|---|------|-----------|
| 1 | AI API 에러를 삼키고 정상 데이터로 저장 | `AiModelServiceImpl.callAiApi()`에서 예외 발생 시 `throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR)` 전파. 또는 도메인 전용 `ReportErrorCode._AI_ANALYSIS_FAILED` 생성 |
| 2 | `StressReportsService.getReportDetail()` NPE | `orElse(null)` → `orElseThrow(() → new GeneralException(ErrorStatus._REPORT_NOT_FOUND))` 변경 |
| 3 | `AttachmentReportsService.getReportDetail()` 소유권 미검증 | `report.getUser().getId().equals(userId)` 검증 추가 |
| 4 | Controller에서 `diagnoseRepository` 직접 호출 | 진단 조회 로직을 각 Service의 `analyzeAndSave()` 메서드 내부로 이동 |
| 5 | `_UNAUTHORIZED(401)` → `_FORBIDDEN(403)` | 권한 검증 실패 시 `ErrorStatus._FORBIDDEN` 또는 도메인 에러코드 `_REPORT_ACCESS_DENIED(403)` 사용 |

### 🟡 개선 권장

| # | 이슈 | 수정 방법 |
|---|------|-----------|
| 6 | 매직 넘버 하드코딩 | `ScoreThresholds` 상수 클래스 또는 enum으로 추출 |
| 7 | `ReportsResponse.java` 비대화 | 리포트 유형별 DTO 파일 분리 (`DepressionAnxietyResDTO`, `StressBurnoutResDTO`, `AttachmentResDTO`) |
| 8 | AI 결과 구분자 `"||"` | 별도 컬럼으로 분리하거나, JSON 형태로 저장 |
| 9 | `System.err.println` | `@Slf4j` + `log.error()` 사용 |
| 10 | `diagnoseType` 불일치 검증 | Service에서 `diagnose.getTestType() == DiagnoseType.XXX` 검증 추가 |

---

## � 교차 도메인 분석에서 발견된 추가 사항 (Global 인프라 기반)

> 🆕 **Global 인프라 분석 (전체 도메인 공통 영향)**

| # | 항목 | Report 도메인 영향 |
|---|------|---|
| 1 | **`JwtAuthenticationFilter`에서 Role 미검증** | `GUEST` 사용자도 리포트 생성/조회 API 호출 가능. 리포트는 진단 완료 후 생성하는 기능이므로, 최소 `USER` Role 필요 |
| 2 | **`DevAuthController` 프로덕션 잔존** | 임의 토큰으로 리포트 API 접근 가능 — 전 도메인 공통 보안 리스크 |
| 3 | **AI 클라이언트 이원화 상세** | `AiConfig`에서 `ChatClient`와 `RestTemplate` 모두 Bean 등록. Report의 `AiModelServiceImpl`은 `RestTemplate`으로 OpenAI 직접 호출, Counsel은 `ChatClient` 사용. `AiConfig`에서 둘 다 등록하므로 의도적이지만, `ChatClient` 통일이 적절 |
| 4 | **`ErrorStatus` 네이밍 불일관** | Report 관련 에러코드 `_REPORT_NOT_FOUND`는 `_` 접두사 사용, `COUNSELOR_NOT_FOUND`는 접두사 없음. `ErrorStatus` enum 전체에서 네이밍 컨벤션 불일관 |

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
- Report 도메인은 6개 엔드포인트로 구성되어 API 범위가 넓지만, **에러 핸들링**(AI 에러 삼킴, NPE)에서 주요 감점 요인이 존재합니다.
- 1-2 항목의 **정상 동작 여부**(20점)와 **에러 핸들링 확인**(15점)은 실제 Swagger 테스트를 통해 최종 평가되어야 합니다.
- 예상 점수는 평가자의 기준에 따라 달라질 수 있으며, 참고용으로만 활용하시기 바랍니다.
