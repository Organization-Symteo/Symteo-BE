# 📋 Diagnose 도메인 — API 평가 기준 정적 분석 보고서

> **분석 대상**: `com.symteo.domain.diagnose`  
> **분석 일시**: 2025-02-07  
> **분석 범위**: 평가 기준 1-1 (API 설계 완성도) + 1-2 (API 구현 완성도 — 정적 분석 가능 범위)

---

## 1-1. API 설계 완성도 (총 20점)

---

### ① RESTful 원칙 준수 (5점)

#### 엔드포인트 목록

| HTTP 메서드 | URL 패턴 | 기능 | RESTful 적합성 |
|---|---|---|---|
| `POST` | `/api/v1/diagnoses` | 검사 생성 | ✅ 리소스 생성에 부합 |
| `GET` | `/api/v1/diagnoses` | 전체 검사 조회 | ✅ 컬렉션 조회에 적합 |
| `GET` | `/api/v1/diagnoses/{diagnoseId}` | 단일 검사 조회 | ✅ 단일 리소스 조회에 적합 |
| `DELETE` | `/api/v1/diagnoses/{diagnoseId}` | 검사 삭제 | ✅ 리소스 삭제에 적합 |

#### 상세 분석

**✅ 양호 항목**
- 기본 URL `@RequestMapping("/api/v1/diagnoses")`이 리소스 중심(명사, 복수형)으로 설계됨
- `POST`, `GET`, `DELETE` HTTP 메서드가 CRUD 의미에 정확히 매칭
- Path Variable(`/{diagnoseId}`)을 통한 개별 리소스 식별이 올바름
- API 버전 관리(`v1`)가 URL에 포함됨
- 엔드포인트가 4개로 간결하며, 불필요한 action-based URL이 없음

**⚠️ 개선 필요 항목**

| # | 항목 | 현재 상태 | 권장 사항 | 감점 요인 |
|---|------|-----------|-----------|-----------|
| — | — | — | — | 특별한 RESTful 위반 사항 없음 |

> 💡 diagnose 도메인의 RESTful 설계는 모범적입니다. 4개 엔드포인트 모두 리소스 중심 URL + 적절한 HTTP 메서드를 사용합니다.

#### 예상 점수: **5.0 / 5점**

---

### ② 공통 응답 포맷 사용 (5점)

#### 응답 구조 분석

| 엔드포인트 | 반환 타입 | 공통 포맷 준수 |
|---|---|---|
| `POST /diagnoses` | `ApiResponse<DiagnoseResDTO.CreateDTO>` | ✅ |
| `GET /diagnoses` | `ApiResponse<List<DiagnoseResDTO.ResultDTO>>` | ✅ |
| `GET /diagnoses/{diagnoseId}` | `ApiResponse<DiagnoseResDTO.ResultDTO>` | ✅ |
| `DELETE /diagnoses/{diagnoseId}` | `ApiResponse<DiagnoseResDTO.DeleteDTO>` | ✅ |

**✅ 양호 항목**
- 모든 엔드포인트가 `ApiResponse<T>`를 일관되게 반환 — **counsel 도메인의 `saveSettings()` 문제 없음**
- 생성/조회/삭제 각각에 대해 전용 응답 DTO(`CreateDTO`, `ResultDTO`, `DeleteDTO`)를 분리하여 사용
- 실패 응답도 `ExceptionAdvice` → `ApiResponse.onFailure()` 경로로 통일 처리됨

**⚠️ 개선 필요 항목**

| # | 항목 | 상세 |
|---|------|------|
| — | — | 특별한 위반 사항 없음 |

#### 예상 점수: **5.0 / 5점**

---

### ③ 파라미터 처리 일관성 (5점)

#### 파라미터 바인딩 분석

| 엔드포인트 | 파라미터 방식 | 적합성 |
|---|---|---|
| `POST /diagnoses` | `@AuthenticationPrincipal Long userId` + `@RequestBody DiagnoseReqDTO.DiagnoseDTO` | ✅ |
| `GET /diagnoses` | `@AuthenticationPrincipal Long userId` | ✅ |
| `GET /diagnoses/{diagnoseId}` | `@AuthenticationPrincipal Long userId` + `@PathVariable Long diagnoseId` | ✅ |
| `DELETE /diagnoses/{diagnoseId}` | `@AuthenticationPrincipal Long userId` + `@PathVariable Long diagnoseId` | ✅ |

**✅ 양호 항목**
- 모든 엔드포인트에서 `@AuthenticationPrincipal Long userId`를 일관되게 사용 (counsel 도메인의 `saveSettings` 문제 없음)
- 조회/삭제에 `@PathVariable`, 생성에 `@RequestBody`로 일관적 구분
- record 타입 DTO로 필요한 필드만 정의 (`DiagnoseDTO`, `AnswerDTO`)

**⚠️ 개선 필요 항목**

| # | 항목 | 상세 |
|---|------|------|
| 1 | **`@Valid` 미적용** | `@RequestBody DiagnoseReqDTO.DiagnoseDTO`에 `@Valid`가 없음. `testType`이 null이거나 `answers`가 빈 리스트여도 검증 없이 통과 |
| 2 | **DTO 필드 검증 어노테이션 부재** | `DiagnoseDTO`의 `testType`에 `@NotNull`, `answers`에 `@NotEmpty`/`@Size` 등이 미적용 |
| 3 | **`AnswerDTO` 점수 범위 검증 부재** | `AnswerDTO.score()`에 `@Min(0)`, `@Max(4)` 같은 범위 제한이 없어 비정상 점수 입력 가능 |

#### 예상 점수: **3.5 / 5점**

---

### ④ 에러 핸들링 (5점)

#### 에러 코드 체계

**도메인 전용 에러코드 (`DiagnoseErrorCode`):**

| 에러 코드 | HTTP 상태 | 코드 문자열 | 메시지 |
|---|---|---|---|
| `_DIAGNOSE_ACCESS_DENIED` | `403 FORBIDDEN` | `DIAGNOSE403` | 해당 진단에 권한이 없습니다 |
| `_DIAGNOSE_NOT_FOUND` | `404 NOT_FOUND` | `DIAGNOSE404` | 해당 분야의 진단이 존재하지 않습니다 |

**✅ 양호 항목**
- 도메인 전용 에러코드 enum (`DiagnoseErrorCode implements BaseErrorCode`) 구현
- 커스텀 예외 클래스 (`DiagnoseException extends GeneralException`) 활용
- HTTP 상태 코드가 의미에 정확히 매핑 (403 권한 없음, 404 찾을 수 없음)
- `deleteDiagnose()`, `getDiagnose()`에서 소유권 검증 (`userId` 일치 여부 확인) 구현
- 글로벌 에러코드와 도메인 에러코드가 잘 분리되어 있음 (counsel 도메인처럼 `ErrorStatus`에 혼재하지 않음)

**⚠️ 개선 필요 항목**

| # | 항목 | 상세 |
|---|------|------|
| 1 | **`@Transactional` 미적용** | `DiagnoseCommandServiceImpl.createDiagnose()`와 `deleteDiagnose()`에 `@Transactional` 어노테이션이 없음. 특히 `deleteDiagnose()`는 조회 후 삭제의 2단계 작업이므로 트랜잭션이 필요 |
| 2 | **타 도메인 예외 import** | `DiagnoseQueryServiceImpl`에서 `CounselErrorCode`, `CounselException`을 import하고 있음 — 사용하지 않는 import이지만, 도메인 간 결합을 암시하며 코드 정리 필요 |
| 3 | **`getAllDiagnose()` 빈 결과 처리** | 진단 이력이 없는 사용자의 경우 `findAllByUserId()`가 빈 리스트를 반환 → `orElseThrow()`로 `_DIAGNOSE_NOT_FOUND` 발생. 빈 리스트는 정상 케이스이므로 예외가 아닌 빈 리스트 반환이 더 적절 |

#### 예상 점수: **4.0 / 5점**

---

### 📊 1-1 종합 점수 (정적 분석 기반 예상)

| 평가 항목 | 배점 | 예상 점수 | 비고 |
|---|---|---|---|
| RESTful 원칙 준수 | 5점 | **5.0점** | 모범적 RESTful 설계 |
| 공통 응답 포맷 사용 | 5점 | **5.0점** | 전 엔드포인트 ApiResponse 통일 |
| 파라미터 처리 일관성 | 5점 | **3.5점** | @Valid 미적용, 필드 검증 부재 |
| 에러 핸들링 | 5점 | **4.0점** | @Transactional 누락, 빈 결과 처리 |
| **합계** | **20점** | **17.5점** | |

---

## 1-2. API 구현 완성도 — 정적 분석 가능 범위

> ⚠️ 1-2 항목은 본래 **실제 서버 실행(Swagger 테스트)** 을 통해 평가하는 항목입니다.  
> 아래는 코드만으로 추론 가능한 부분을 정리한 것이며, 실제 점수와 차이가 있을 수 있습니다.

---

### ① 정상 동작 여부 — 코드 레벨 위험 요소 (20점)

| # | 위험 요소 | 위치 | 심각도 | 상세 |
|---|-----------|------|--------|------|
| 1 | **Entity에 `@Setter` 사용** | `Diagnose.java` | 🟡 중간 | JPA 엔티티에 `@Setter`가 적용됨. 불변성 위반이며, 의도치 않은 상태 변경 위험. `@Setter` 대신 업데이트용 비즈니스 메서드 제공 권장 |
| 2 | **`@RequiredArgsConstructor` + `@AllArgsConstructor` 공존** | `Diagnose.java` | 🟡 중간 | 두 생성자 어노테이션이 동시에 존재. `@Builder` + `@NoArgsConstructor(access = PROTECTED)` + `@AllArgsConstructor(access = PROTECTED)` 조합이 JPA 표준 |
| 3 | **`@Transactional` 미적용** | `DiagnoseCommandServiceImpl` | 🔴 높음 | `createDiagnose()`와 `deleteDiagnose()`에 트랜잭션이 없어 데이터 정합성 문제 발생 가능. 특히 삭제 시 조회-검증-삭제 과정에서 동시성 이슈 위험 |
| 4 | **미사용 import** | `DiagnoseQueryServiceImpl` | 🟢 낮음 | `CounselErrorCode`, `CounselException` import가 남아 있음 — 동작에는 영향 없으나 코드 품질 저하 |
| 5 | **JSON 컬럼 의존성** | `Diagnose.answers` | 🟢 낮음 | `@JdbcTypeCode(SqlTypes.JSON)`으로 답변을 JSON 배열로 저장. DB 종류에 따라 JSON 지원 여부가 다를 수 있음 (MySQL 5.7+ / PostgreSQL OK) |

### ② 에러 핸들링 확인 — 코드 레벨 분석 (15점)

| 시나리오 | 코드 대응 여부 | 상세 |
|---|---|---|
| 존재하지 않는 diagnoseId 전송 | ✅ 처리됨 | `diagnoseRepository.findById().orElseThrow(DiagnoseException)` |
| 타인의 diagnose 접근 (조회) | ✅ 처리됨 | `getDiagnose()`에서 `userId` 일치 검증 후 `_DIAGNOSE_ACCESS_DENIED` |
| 타인의 diagnose 접근 (삭제) | ✅ 처리됨 | `deleteDiagnose()`에서 `userId` 일치 검증 후 `_DIAGNOSE_ACCESS_DENIED` |
| 미인증 요청 | ✅ 처리됨 | 모든 엔드포인트에 `@AuthenticationPrincipal` 적용 |
| null testType 전송 | ❌ 미처리 | DTO에 `@NotNull`/`@Valid` 없음. DB INSERT 시 null 허용(`@Column`에 nullable 미설정)이므로 null 그대로 저장 |
| 빈 answers 리스트 전송 | ❌ 미처리 | `@NotEmpty`/`@Size` 미적용. 빈 배열이 JSON으로 저장 가능 |
| 음수/비정상 score 전송 | ❌ 미처리 | `AnswerDTO.score()`에 범위 검증 없음 |
| 진단 이력 없는 사용자 조회 | ⚠️ 과잉 처리 | `getAllDiagnose()`에서 빈 리스트를 예외로 처리. 정상적으로 빈 리스트를 반환하는 것이 적절 |

### ③ 참고 서류 (5점)

> 📌 이 항목은 Notion, 영상, README 등 외부 문서로 평가되므로 정적 분석 범위 밖입니다.

---

## 🔧 발견된 주요 이슈 및 개선 권고

### 🔴 즉시 수정 권장 (감점 직결)

| # | 이슈 | 수정 방법 |
|---|------|-----------|
| 1 | `@Transactional` 미적용 | `DiagnoseCommandServiceImpl.createDiagnose()`와 `deleteDiagnose()`에 `@Transactional` 추가. 또는 클래스 레벨에 `@Transactional` 적용 |
| 2 | `@Valid` 미적용 | `DiagnoseController.askDiagnose()`의 `@RequestBody` 앞에 `@Valid` 추가 |
| 3 | DTO 필드 검증 추가 | `DiagnoseReqDTO.DiagnoseDTO`에 `@NotNull DiagnoseType testType`, `@NotEmpty List<AnswerDTO> answers` 적용 |

### 🟡 개선 권장

| # | 이슈 | 수정 방법 |
|---|------|-----------|
| 4 | Entity `@Setter` 제거 | `Diagnose`에서 `@Setter` 제거, 필요 시 별도 업데이트 메서드 작성 |
| 5 | `@RequiredArgsConstructor` 제거 | Entity에서 `@RequiredArgsConstructor` 제거, `@NoArgsConstructor(access = PROTECTED)` 사용 |
| 6 | 미사용 import 정리 | `DiagnoseQueryServiceImpl`에서 `CounselErrorCode`, `CounselException` import 제거 |
| 7 | `getAllDiagnose()` 빈 리스트 처리 | `orElseThrow()` 대신 `orElse(Collections.emptyList())` 또는 반환 타입을 `List<>` 직접 반환으로 변경 |
| 8 | `AnswerDTO` 점수 검증 | `@Min(0) @Max(4) Long score` 등 범위 제한 추가 |

---

## 📊 Counsel vs Diagnose 도메인 비교

| 평가 항목 | Counsel | Diagnose | 비고 |
|---|---|---|---|
| RESTful 원칙 준수 | 3.5 / 5 | **5.0 / 5** | diagnose가 더 깔끔한 RESTful 설계 |
| 공통 응답 포맷 사용 | 4.0 / 5 | **5.0 / 5** | diagnose는 전 엔드포인트 ApiResponse 통일 |
| 파라미터 처리 일관성 | 3.0 / 5 | **3.5 / 5** | 둘 다 @Valid 미적용이지만 diagnose는 인증 일관성 유지 |
| 에러 핸들링 | 4.0 / 5 | 4.0 / 5 | 동일 수준. diagnose는 @Transactional 누락, counsel은 권한검증 일부 누락 |
| **합계** | **14.5 / 20** | **17.5 / 20** | |

---

## � 교차 도메인 분석에서 발견된 추가 사항

> `report` 도메인 분석을 통해 발견된 diagnose 도메인 관련 추가 인사이트

| # | 항목 | 상세 |
|---|------|------|
| 1 | **진단 데이터가 리포트 도메인에서 직접 소비됨** | `ReportsController`에서 `DiagnoseRepository.findById()`를 Controller 레벨에서 직접 호출. 도메인 간 결합도가 높음. Diagnose 도메인에 `DiagnoseQueryService.getDiagnoseEntity(Long id)` 같은 내부 조회 메서드를 제공하여 캡슐화하는 것이 적절 |
| 2 | **`DiagnoseType` 불일치 검증 부재** | 우울/불안 진단 결과(`DEPRESSION_ANXIETY_COMPLEX`)로 스트레스 리포트를 생성할 수 있으나, 아무런 검증이 없음. `Diagnose.testType`과 요청된 리포트 유형의 일치 여부 검증이 diagnose 또는 report 어느 한쪽에서 필요 |
| 3 | **`AnswerDTO`에 `questionNo` 범위 검증 부재** | report 도메인에서 `questionNo`로 문항을 필터링하여 점수를 계산. 잘못된 `questionNo` 값이 들어오면 점수 계산이 잘못됨. diagnose 단에서 `AnswerDTO.questionNo` 범위 제한이 필요 |
| 4 | **`DiagnoseReqDTO.AnswerDTO`가 report Entity에서도 사용됨** | `Diagnose.answers` 필드가 `List<DiagnoseReqDTO.AnswerDTO>` 타입. DTO가 Entity 필드 타입으로 사용되어 계층 간 결합도 증가. 공통 VO 또는 Entity 전용 embeddable로 분리 권장 |

> 🆕 **Global 인프라 분석 (전체 도메인 공통 영향)**

| # | 항목 | Diagnose 도메인 영향 |
|---|------|---|
| 5 | **`JwtAuthenticationFilter`에서 Role 미검증** | `GUEST` 사용자(회원가입 미완료)도 진단 API 호출 가능. 진단은 회원 전용 기능이어야 함 |
| 6 | **`DevAuthController` 프로덕션 잔존** | 임의 토큰으로 진단 API 접근 가능 — 전 도메인 공통 보안 리스크 |
| 7 | **`@Valid` 미적용** | `DiagnoseReqDTO`에 `@Valid` 없음. `answers` 리스트의 개수, `questionNo` 범위 등 서버 사이드 검증 부재 |
| 8 | **`@Transactional` 적용 불일관 (프로젝트 전체)** | Diagnose는 `CommandServiceImpl`에 `@Transactional` 미적용. 반면 User는 클래스 레벨 적용, Report도 클래스 레벨 적용. 6개 도메인 중 3가지 패턴 혼재 |

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
- 1-2 항목의 **정상 동작 여부**(20점)와 **에러 핸들링 확인**(15점)은 실제 Swagger 테스트 또는 통합 테스트를 통해 최종 평가되어야 합니다.
- 예상 점수는 평가자의 기준에 따라 달라질 수 있으며, 참고용으로만 활용하시기 바랍니다.
