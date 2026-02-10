# 📋 Diagnose 도메인 — API 평가 기준 정적 분석 보고서 v2

> **분석 대상**: `com.symteo.domain.diagnose`  
> **분석 일시**: 2025-02-09  
> **이전 버전**: diagnose-convention-v1.md (2025-02-07)  
> **분석 범위**: 평가 기준 1-1 (API 설계 완성도) + 1-2 (API 구현 완성도 — 정적 분석 가능 범위)

---

## 📌 v1 대비 주요 변경 사항 요약

| # | 이슈 (v1) | 상태 | 변경 내용 |
|---|-----------|------|-----------|
| 1 | `@Valid` 미적용 | ✅ **해결** | `DiagnoseController.askDiagnose()`에 `@RequestBody @Valid` 적용 |
| 2 | DTO 필드 검증 어노테이션 부재 | ✅ **해결** | `DiagnoseDTO.testType`에 `@NotBlank`, `answers`에 `@NotEmpty`, `AnswerDTO` 필드에 `@NotNull` 적용 |
| 3 | `@Transactional` 미적용 | ✅ **해결** | `DiagnoseCommandServiceImpl`의 `createDiagnose()`와 `deleteDiagnose()`에 `@Transactional` 적용 |
| 4 | Entity에 `@Setter` 사용 | ⚪ **v1 오류** | 재확인 결과 `@Setter`는 원래 존재하지 않았음. v1 보고서의 오탐 |
| 5 | `AnswerDTO` 점수 범위 검증 부재 | ❌ **미해결** | `@Min(0)`, `@Max(4)` 같은 범위 제한이 여전히 미적용 |
| 6 | 타 도메인 예외 import | ❌ **미해결** | `DiagnoseQueryServiceImpl`에 `CounselErrorCode`, `CounselException` import 잔존 |
| 7 | `getAllDiagnose()` 빈 결과 처리 | ❌ **미해결** | 빈 리스트를 예외로 처리하는 로직 그대로 유지 |
| 8 | `@NotBlank`을 Enum 타입에 사용 | 🆕 **신규** | `DiagnoseDTO.testType`에 `@NotBlank` 적용 — Enum 타입에는 `@NotNull`이 올바름 |

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

> 💡 v1과 동일하게 Diagnose 도메인의 RESTful 설계는 모범적입니다. 4개 엔드포인트 모두 리소스 중심 URL + 적절한 HTTP 메서드를 사용합니다.

#### 예상 점수: **5.0 / 5점** (v1: 5.0점 → 변동 없음)

---

### ② 공통 응답 포맷 사용 (5점)

#### 응답 구조 분석

**`ApiResponse<T>` 공통 응답 포맷:**
```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "성공입니다.",
  "result": { ... }
}
```

| 엔드포인트 | 반환 타입 | 공통 포맷 준수 |
|---|---|---|
| `POST /diagnoses` | `ApiResponse<DiagnoseResDTO.CreateDTO>` | ✅ |
| `GET /diagnoses` | `ApiResponse<List<DiagnoseResDTO.ResultDTO>>` | ✅ |
| `GET /diagnoses/{diagnoseId}` | `ApiResponse<DiagnoseResDTO.ResultDTO>` | ✅ |
| `DELETE /diagnoses/{diagnoseId}` | `ApiResponse<DiagnoseResDTO.DeleteDTO>` | ✅ |

**✅ 양호 항목**
- 모든 엔드포인트가 `ApiResponse<T>`를 일관되게 반환
- 생성/조회/삭제 각각에 대해 전용 응답 DTO(`CreateDTO`, `ResultDTO`, `DeleteDTO`)를 분리하여 사용
- 실패 응답도 `ExceptionAdvice` → `ApiResponse.onFailure()` 경로로 통일 처리됨

**실패 응답 포맷:**
- `ExceptionAdvice`에서 `GeneralException`(→ `DiagnoseException`)을 catch하여 `ApiResponse.onFailure(code, message, null)` 형태로 통일 반환 → ✅
- `ConstraintViolationException`, `MethodArgumentNotValidException`도 동일 포맷으로 처리 → ✅

#### 예상 점수: **5.0 / 5점** (v1: 5.0점 → 변동 없음)

---

### ③ 파라미터 처리 일관성 (5점)

#### 파라미터 바인딩 분석

| 엔드포인트 | 파라미터 방식 | 적합성 |
|---|---|---|
| `POST /diagnoses` | `@AuthenticationPrincipal Long userId` + `@RequestBody @Valid DiagnoseReqDTO.DiagnoseDTO` | ✅ **v1 대비 개선** |
| `GET /diagnoses` | `@AuthenticationPrincipal Long userId` | ✅ |
| `GET /diagnoses/{diagnoseId}` | `@AuthenticationPrincipal Long userId` + `@PathVariable Long diagnoseId` | ✅ |
| `DELETE /diagnoses/{diagnoseId}` | `@AuthenticationPrincipal Long userId` + `@PathVariable Long diagnoseId` | ✅ |

**✅ 양호 항목**
- 모든 엔드포인트에서 `@AuthenticationPrincipal Long userId`를 일관되게 사용
- 조회/삭제에 `@PathVariable`, 생성에 `@RequestBody`로 일관적 구분
- record 타입 DTO로 필요한 필드만 정의 (`DiagnoseDTO`, `AnswerDTO`)
- **[v1 대비 개선]** `@Valid`가 `@RequestBody` 파라미터에 적용됨
- **[v1 대비 개선]** `DiagnoseDTO.answers`에 `@NotEmpty` 적용 — 빈 리스트 입력 차단
- **[v1 대비 개선]** `AnswerDTO.questionNo`와 `score`에 `@NotNull` 적용

**⚠️ 잔존 이슈**

| # | 항목 | 상세 |
|---|------|------|
| 1 | **`@NotBlank`을 Enum 타입에 사용** | `DiagnoseDTO.testType`에 `@NotBlank(message = "테스트 타입을 적어주세요.")`가 적용됨. `@NotBlank`는 `CharSequence`(String) 전용 어노테이션이므로 `DiagnoseType` enum에 적용하면 **런타임에 `UnexpectedTypeException` 발생**. `@NotNull`로 변경해야 함 |
| 2 | **`AnswerDTO.score` 범위 검증 부재** | `@NotNull`은 적용되었으나, `@Min(0)` / `@Max(4)` 같은 범위 제한이 없어 음수나 비정상 점수 입력 가능 |
| 3 | **`AnswerDTO.questionNo` 범위 검증 부재** | `@NotNull`만 적용. Report 도메인에서 `questionNo`로 문항 필터링 → 점수 계산에 사용하므로, 잘못된 범위의 `questionNo`가 오면 점수 계산 오류 발생 |

#### 예상 점수: **3.5 / 5점** (v1: 3.5점 → 변동 없음)

> **점수 유지 근거**: `@Valid` 적용과 `@NotEmpty`/`@NotNull` 추가는 개선이지만, `@NotBlank`의 Enum 타입 오적용이 **런타임 에러를 유발하는 심각한 이슈**로 상쇄됨. `@NotBlank`을 Enum에 사용하면 검사 생성 API 자체가 500 에러를 반환하게 될 수 있음.

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
- 글로벌 에러코드와 도메인 에러코드가 잘 분리되어 있음
- **[v1 대비 개선]** `DiagnoseCommandServiceImpl`에 `@Transactional` 적용 — 데이터 정합성 확보

**⚠️ 잔존 이슈**

| # | 항목 | 상세 |
|---|------|------|
| 1 | **타 도메인 예외 import** | `DiagnoseQueryServiceImpl`에서 `CounselErrorCode`, `CounselException`을 import — 사용하지 않는 import이며 도메인 간 불필요한 결합을 암시. 코드 정리 필요 |
| 2 | **`getAllDiagnose()` 빈 결과 처리** | 진단 이력이 없는 사용자의 경우 `findAllByUserId()`가 빈 리스트를 반환 → `orElseThrow()`로 `_DIAGNOSE_NOT_FOUND` 발생. 빈 리스트는 정상 케이스이므로 예외가 아닌 빈 리스트 반환이 적절 |
| 3 | **에러코드 중복** | `DiagnoseErrorCode._DIAGNOSE_NOT_FOUND`("해당 분야의 진단이 존재하지 않습니다")와 글로벌 `ErrorStatus._DIAGNOSE_NOT_FOUND`("검사 결과가 없습니다")가 동일한 `DIAGNOSE404` 코드를 사용하지만 메시지가 다름. `ReportsController`에서 글로벌 에러코드를 사용하고 있어 일관성 부족 |
| 4 | **`DiagnoseQueryServiceImpl`에 `@Transactional(readOnly = true)` 미적용** | 읽기 전용 서비스에 `@Transactional(readOnly = true)` 적용 시 성능 최적화(Dirty Checking 스킵, 읽기 전용 커넥션 라우팅) 가능 |

#### 예상 점수: **4.5 / 5점** (v1: 4.0점 → +0.5점 상승)

> **점수 상승 근거**: `@Transactional` 적용으로 데이터 정합성 문제가 해결됨. 미사용 import, 빈 리스트 예외 처리, 에러코드 중복은 기능에 치명적이지 않은 경미한 이슈.

---

### 📊 1-1 종합 점수 (정적 분석 기반 예상)

| 평가 항목 | 배점 | v1 점수 | v2 점수 | 변동 | 비고 |
|---|---|---|---|---|---|
| RESTful 원칙 준수 | 5점 | 5.0점 | **5.0점** | — | 모범적 RESTful 설계 유지 |
| 공통 응답 포맷 사용 | 5점 | 5.0점 | **5.0점** | — | 전 엔드포인트 ApiResponse 통일 유지 |
| 파라미터 처리 일관성 | 5점 | 3.5점 | **3.5점** | — | `@Valid` 추가 개선되었으나 `@NotBlank` Enum 오적용으로 상쇄 |
| 에러 핸들링 | 5점 | 4.0점 | **4.5점** | +0.5 | `@Transactional` 적용 |
| **합계** | **20점** | **17.5점** | **18.0점** | **+0.5** | |

---

## 1-2. API 구현 완성도 — 정적 분석 가능 범위

> ⚠️ 1-2 항목은 본래 **실제 서버 실행(Swagger 테스트)** 을 통해 평가하는 항목입니다.  
> 아래는 코드만으로 추론 가능한 부분을 정리한 것이며, 실제 점수와 차이가 있을 수 있습니다.

---

### ① 정상 동작 여부 — 코드 레벨 위험 요소 (20점)

| # | 위험 요소 | 위치 | 심각도 | 상태 | 상세 |
|---|-----------|------|--------|------|------|
| 1 | **`@NotBlank`을 Enum 타입에 사용** | `DiagnoseReqDTO.DiagnoseDTO.testType` | 🔴 높음 | 🆕 **신규** | `@NotBlank`는 `CharSequence` 타입 전용. `DiagnoseType` enum에 적용 시 `jakarta.validation.UnexpectedTypeException` 발생하여 **검사 생성 API가 500 에러 반환**. `@NotNull`로 변경 필요 |
| 2 | ~~`@Transactional` 미적용~~ | `DiagnoseCommandServiceImpl` | — | ✅ **해결** | `createDiagnose()`, `deleteDiagnose()` 모두 `@Transactional` 적용 완료 |
| 3 | **`@RequiredArgsConstructor` + `@AllArgsConstructor` 공존** | `Diagnose.java` | 🟡 중간 | ❌ **미해결** | 두 생성자 어노테이션이 동시에 존재. JPA 표준은 `@NoArgsConstructor(access = PROTECTED)` + `@AllArgsConstructor(access = PRIVATE)` + `@Builder` 조합. `@RequiredArgsConstructor`는 `final` 필드가 없으면 기본 생성자와 동일하나, JPA 프록시 생성 시 `@NoArgsConstructor`가 명시적으로 필요 |
| 4 | **미사용 import** | `DiagnoseQueryServiceImpl` | 🟢 낮음 | ❌ **미해결** | `CounselErrorCode`, `CounselException` import가 남아 있음 — 동작에는 영향 없으나 코드 품질 저하 |
| 5 | **JSON 컬럼 의존성** | `Diagnose.answers` | 🟢 낮음 | ❌ **미해결** | `@JdbcTypeCode(SqlTypes.JSON)`으로 답변을 JSON 배열로 저장. DB 종류에 따라 JSON 지원 여부가 다를 수 있음 (MySQL 5.7+ / PostgreSQL OK) |
| 6 | **DTO가 Entity 필드 타입으로 사용** | `Diagnose.answers` | 🟡 중간 | ❌ **미해결** | `List<DiagnoseReqDTO.AnswerDTO>` 타입이 Entity 필드 타입으로 사용됨. DTO가 영속 계층에 침투하여 계층 간 결합도 증가. 공통 VO 또는 Entity 전용 embeddable로 분리 권장 |
| 7 | ~~Entity에 `@Setter` 사용~~ | `Diagnose.java` | — | ⚪ **v1 오탐** | 재확인 결과 `@Setter`는 원래 존재하지 않았음. `@Builder` + `@Getter`만 사용하여 불변성 유지 |

### ② 에러 핸들링 확인 — 코드 레벨 분석 (15점)

| 시나리오 | 코드 대응 여부 | v1 대비 변화 | 상세 |
|---|---|---|---|
| 존재하지 않는 diagnoseId 전송 | ✅ 처리됨 | 유지 | `diagnoseRepository.findById().orElseThrow(DiagnoseException)` |
| 타인의 diagnose 접근 (조회) | ✅ 처리됨 | 유지 | `getDiagnose()`에서 `userId` 일치 검증 후 `_DIAGNOSE_ACCESS_DENIED` |
| 타인의 diagnose 접근 (삭제) | ✅ 처리됨 | 유지 | `deleteDiagnose()`에서 `userId` 일치 검증 후 `_DIAGNOSE_ACCESS_DENIED` |
| 미인증 요청 | ✅ 처리됨 | 유지 | 모든 엔드포인트에 `@AuthenticationPrincipal` 적용 |
| null testType 전송 | ⚠️ 오류 있음 | **신규 이슈** | `@NotBlank`이 Enum에 적용됨 → `UnexpectedTypeException` (500) 발생. `@NotNull`이어야 정상 400 응답 |
| 빈 answers 리스트 전송 | ✅ 처리됨 | **개선** | `@NotEmpty(message = "리스트가 비어있을 수 없습니다.")` 적용으로 빈 리스트 차단 |
| null questionNo/score 전송 | ✅ 처리됨 | **개선** | `@NotNull` 적용으로 null 값 차단 |
| 음수/비정상 score 전송 | ❌ 미처리 | **미해결** | `AnswerDTO.score()`에 범위 검증 없음 |
| 진단 이력 없는 사용자 조회 | ⚠️ 과잉 처리 | **미해결** | `getAllDiagnose()`에서 빈 리스트를 예외로 처리. 정상적으로 빈 리스트를 반환하는 것이 적절 |
| 잘못된 testType 문자열 전송 | ✅ 처리됨 | 유지 | Enum 매핑 실패 시 `HttpMessageNotReadableException` → `ExceptionAdvice`에서 한글 메시지 반환 |

### ③ 참고 서류 (5점)

> 📌 이 항목은 Notion, 영상, README 등 외부 문서로 평가되므로 정적 분석 범위 밖입니다.

| 확인 가능 항목 | 상태 |
|---|---|
| 기술 스택 선택 이유 문서 | ❓ README.md 확인 필요 |
| API 설계 구조 설명 문서 | ❓ Notion 등 외부 문서 확인 필요 |

---

## 🔧 현재 잔존 이슈 및 개선 권고

### 🔴 즉시 수정 권장 (감점 직결)

| # | 이슈 | 수정 방법 | 관련 파일 |
|---|------|-----------|-----------|
| 1 | `@NotBlank`을 Enum 타입에 사용 — 런타임 `UnexpectedTypeException` 발생 | `@NotBlank(message = "테스트 타입을 적어주세요.")` → `@NotNull(message = "테스트 타입을 적어주세요.")`로 변경 | `DiagnoseReqDTO.java` |
| 2 | `getAllDiagnose()` 빈 결과를 예외로 처리 | `orElseThrow()` 제거. `findAllByUserId()`의 반환 타입을 `List<Diagnose>`로 변경하거나, `orElse(Collections.emptyList())` 사용 | `DiagnoseQueryServiceImpl.java`, `DiagnoseRepository.java` |

### 🟡 개선 권장

| # | 이슈 | 수정 방법 | 관련 파일 |
|---|------|-----------|-----------|
| 3 | `AnswerDTO` 점수 범위 검증 | `@Min(0) @Max(4) Long score` 등 범위 제한 추가 | `DiagnoseReqDTO.java` |
| 4 | `AnswerDTO` 문항 번호 범위 검증 | `@Min(1) Long questionNo` 등 최소값 제한 추가 | `DiagnoseReqDTO.java` |
| 5 | `@RequiredArgsConstructor` 제거 | Entity에서 `@RequiredArgsConstructor` → `@NoArgsConstructor(access = AccessLevel.PROTECTED)` 사용 | `Diagnose.java` |
| 6 | 미사용 import 정리 | `CounselErrorCode`, `CounselException` import 제거 | `DiagnoseQueryServiceImpl.java` |
| 7 | `DiagnoseQueryServiceImpl`에 `@Transactional(readOnly = true)` 적용 | 클래스 레벨에 `@Transactional(readOnly = true)` 추가하여 읽기 전용 최적화 | `DiagnoseQueryServiceImpl.java` |
| 8 | 에러코드 중복 제거 | `ErrorStatus._DIAGNOSE_NOT_FOUND`를 제거하고, `ReportsController`도 `DiagnoseErrorCode._DIAGNOSE_NOT_FOUND`를 사용하도록 통일 | `ErrorStatus.java`, `ReportsController.java` |
| 9 | DTO-Entity 계층 분리 | `Diagnose.answers` 필드에 `DiagnoseReqDTO.AnswerDTO` 대신 전용 VO/embeddable 타입 사용 | `Diagnose.java`, `DiagnoseReqDTO.java` |

### 🟢 코드 품질 권장

| # | 이슈 | 수정 방법 |
|---|------|-----------|
| 10 | `@AllArgsConstructor` 접근 수준 제한 | `@AllArgsConstructor(access = AccessLevel.PRIVATE)` 으로 변경하여 외부에서 직접 생성자 호출 방지 |
| 11 | `ResultDTO`에 `diagnoseId`, `createdAt` 포함 고려 | 조회 결과에 식별자와 생성 시각이 없으면 클라이언트에서 리소스를 고유하게 식별하거나 정렬할 수 없음 |
| 12 | 소프트 삭제(`deletedAt`) 미사용 | Entity에 `deletedAt` 필드가 있으나 `deleteDiagnose()`에서 물리 삭제(`diagnoseRepository.delete()`)를 수행. 설계 의도에 따라 소프트 삭제 적용 또는 `deletedAt` 필드 제거 |

---

## 📊 교차 도메인 분석에서 발견된 추가 사항

> `report` 도메인 분석을 통해 발견된 diagnose 도메인 관련 인사이트

| # | 항목 | 상세 | v1 대비 |
|---|------|------|---------|
| 1 | **진단 데이터가 리포트 도메인에서 직접 소비됨** | `ReportsController`에서 `DiagnoseRepository.findById()`를 Controller 레벨에서 직접 호출. 도메인 간 결합도가 높음. `DiagnoseQueryService`를 통해 캡슐화하는 것이 적절 | 미해결 |
| 2 | **`ReportsController`에서 소유권 미검증** | `diagnoseRepository.findById(diagnoseId)` 후 `diagnose.getUserId() == userId` 검증 없이 바로 리포트 생성. 타인의 진단 데이터로 리포트 생성 가능 | 미해결 |
| 3 | **`DiagnoseType` 불일치 검증 부재** | 우울/불안 진단 결과로 스트레스 리포트 생성이 가능하나 검증 없음 | 미해결 |
| 4 | **에러코드 중복** | `DiagnoseErrorCode._DIAGNOSE_NOT_FOUND`와 `ErrorStatus._DIAGNOSE_NOT_FOUND` 메시지 불일치 | 🆕 신규 발견 |

> 🆕 **Global 인프라 분석 (전체 도메인 공통 영향)**

| # | 항목 | Diagnose 도메인 영향 | v1 대비 |
|---|------|---|---------|
| 5 | **`JwtAuthenticationFilter`에서 Role 미검증** | `authorities`가 빈 리스트(`new ArrayList<>()`)로 설정. 별도 Role 검증 없이 인증된 사용자 모두 접근 가능 | 미해결 |
| 6 | **`DevAuthController` 미존재** | v1에서 지적된 `DevAuthController`는 확인 결과 존재하지 않음 — 보안 리스크 해당 없음 | ⚪ v1 오탐 |
| 7 | **`@Transactional` 적용 개선** | `DiagnoseCommandServiceImpl`에 `@Transactional` 적용 완료. 다만 `DiagnoseQueryServiceImpl`에는 `@Transactional(readOnly = true)` 미적용 | 부분 해결 |
| 8 | **`SecurityConfig`에 CORS 설정 부재** | 프론트엔드와의 통신 시 CORS 이슈 발생 가능 | 🆕 신규 발견 |

---

## 📊 v1 → v2 점수 변동 총괄

### 1-1. API 설계 완성도

| 평가 항목 | 배점 | v1 점수 | v2 점수 | 변동 |
|---|---|---|---|---|
| RESTful 원칙 준수 | 5점 | 5.0점 | **5.0점** | — |
| 공통 응답 포맷 사용 | 5점 | 5.0점 | **5.0점** | — |
| 파라미터 처리 일관성 | 5점 | 3.5점 | **3.5점** | — |
| 에러 핸들링 | 5점 | 4.0점 | **4.5점** | ▲ +0.5 |
| **합계** | **20점** | **17.5점** | **18.0점** | **▲ +0.5** |

### 1-2. API 구현 완성도 (정적 분석 가능 범위)

| 평가 항목 | 이슈 수(v1) | 이슈 수(v2) | 변동 |
|---|---|---|---|
| 코드 레벨 위험 요소 | 5건 | 5건 (2건 해결, 1건 오탐 확인, 1건 신규 발견) | 소폭 개선 |
| 에러 핸들링 미처리 시나리오 | 4건 | 3건 (2건 개선, 1건 신규) | ▲ 개선 |

---

## 📊 예상 도메인 간 비교 (v2 기준)

| 평가 항목 | Counsel (v2) | Diagnose (v2) | Home | Report | TodayMission | User |
|---|---|---|---|---|---|---|
| RESTful 원칙 준수 | 4.5 / 5 | **5.0 / 5** | 3.5 / 5 | 3.5 / 5 | 2.5 / 5 | 3.5 / 5 |
| 공통 응답 포맷 사용 | 5.0 / 5 | **5.0 / 5** | 4.5 / 5 | 5.0 / 5 | 5.0 / 5 | 4.0 / 5 |
| 파라미터 처리 일관성 | 4.0 / 5 | 3.5 / 5 | 3.0 / 5 | **4.5 / 5** | 3.0 / 5 | 3.5 / 5 |
| 에러 핸들링 | **4.5 / 5** | **4.5 / 5** | 3.0 / 5 | 3.0 / 5 | 3.0 / 5 | 3.5 / 5 |
| **합계** | **18.0 / 20** | **18.0 / 20** | **14.0 / 20** | **16.0 / 20** | **13.5 / 20** | **14.5 / 20** |

> 📈 **Diagnose 도메인이 v2 기준 18.0점으로 Counsel v2와 공동 1위**. v1(17.5점) 대비 +0.5점 상승. `@Transactional` 적용이 주요 개선 요인이나, `@NotBlank` Enum 오적용이 파라미터 처리 점수 상승을 제한.

---

## 📝 비고

- 이 보고서는 **소스 코드 정적 분석**만을 기반으로 작성되었습니다.
- v1에서 지적된 **3개 즉시 수정 권장 이슈 중 2개가 해결**(`@Transactional` 적용, `@Valid` + DTO 검증 추가)되었으며, DTO 검증 과정에서 `@NotBlank` Enum 오적용이라는 신규 이슈가 발생했습니다.
- v1에서 지적된 `@Setter` 사용과 `DevAuthController` 잔존은 재확인 결과 **v1 보고서의 오탐**이었습니다.
- 1-2 항목의 **정상 동작 여부**(20점)와 **에러 핸들링 확인**(15점)은 실제 Swagger 테스트 또는 통합 테스트를 통해 최종 평가되어야 합니다.
- 예상 점수는 평가자의 기준에 따라 달라질 수 있으며, 참고용으로만 활용하시기 바랍니다.
