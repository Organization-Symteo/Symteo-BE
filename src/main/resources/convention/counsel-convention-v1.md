# 📋 Counsel 도메인 — API 평가 기준 정적 분석 보고서

> **분석 대상**: `com.symteo.domain.counsel`  
> **분석 일시**: 2025-02-07  
> **분석 범위**: 평가 기준 1-1 (API 설계 완성도) + 1-2 (API 구현 완성도 — 정적 분석 가능 범위)

---

## 1-1. API 설계 완성도 (총 20점)

---

### ① RESTful 원칙 준수 (5점)

#### 엔드포인트 목록

| HTTP 메서드 | URL 패턴 | 기능 | RESTful 적합성 |
|---|---|---|---|
| `POST` | `/api/v1/counsels` | AI 상담 요청 | ✅ 리소스 생성 의미에 부합 |
| `POST` | `/api/v1/counsels/report` | 리포트 분석 요청 | ⚠️ 아래 상세 설명 |
| `PATCH` | `/api/v1/counsels` | 상담 종료(요약) | ⚠️ 아래 상세 설명 |
| `GET` | `/api/v1/counsels` | 전체 상담 조회 | ✅ 컬렉션 조회에 적합 |
| `GET` | `/api/v1/counsels/{counselId}` | 단일 상담 조회 | ✅ 단일 리소스 조회에 적합 |
| `DELETE` | `/api/v1/counsels/{counselId}` | 상담 삭제 | ✅ 리소스 삭제에 적합 |
| `PUT` | `/api/v1/counsels/setting` | 상담사 초기 설정 저장 | ⚠️ 아래 상세 설명 |

#### 상세 분석

**✅ 양호 항목**
- 기본 URL `@RequestMapping("/api/v1/counsels")`이 리소스 중심(명사, 복수형)으로 설계됨
- `GET`, `POST`, `DELETE`, `PATCH`를 적절히 구분하여 사용
- Path Variable(`/{counselId}`)을 통한 리소스 식별이 올바름
- API 버전 관리(`v1`)가 URL에 포함됨

**⚠️ 개선 필요 항목**

| # | 항목 | 현재 상태 | 권장 사항 | 감점 요인 |
|---|------|-----------|-----------|-----------|
| 1 | 리포트 분석 URL | `POST /api/v1/counsels/report` | 리포트 분석은 상담 생성의 하위 행위(action)로 볼 수 있으나, REST 관점에서 `/api/v1/counsels/report-analysis` 또는 `/api/v1/counsels/{counselId}/report` 처럼 리소스 계층 구조를 더 명확히 하는 것이 좋음 | 경미 |
| 2 | 상담 종료 URL | `PATCH /api/v1/counsels` (Body에 `chatRoomId` 포함) | `PATCH /api/v1/counsels/{counselId}/summary` 형태로 대상 리소스를 URL에 명시하는 것이 RESTful. Body에 ID를 넣는 것은 RPC 스타일에 가까움 | 중간 |
| 3 | 설정 저장 URL | `PUT /api/v1/counsels/setting` | 상담사 설정은 상담 리소스의 하위보다는 별도 리소스. `/api/v1/counselor-settings` 또는 `/api/v1/users/{userId}/counselor-settings`가 더 적합. 또한 `PUT`인데 `@AuthenticationPrincipal` 없이 Body에 `userId`를 받고 있음 | 중간 |

#### 예상 점수: **3.5 / 5점**

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
| `POST /counsels` | `ApiResponse<CounselResDTO.ChatMessage>` | ✅ |
| `POST /counsels/report` | `ApiResponse<CounselResDTO.ChatMessage>` | ✅ |
| `PATCH /counsels` | `ApiResponse<CounselResDTO.ChatSummary>` | ✅ |
| `GET /counsels` | `ApiResponse<List<CounselResDTO.Chat>>` | ✅ |
| `GET /counsels/{counselId}` | `ApiResponse<CounselResDTO.ChatSummary>` | ✅ |
| `DELETE /counsels/{counselId}` | `ApiResponse<Long>` | ✅ |
| `PUT /counsels/setting` | **`ResponseEntity<String>`** | ❌ **비일관** |

**⚠️ 핵심 문제:**
- `saveSettings()` 메서드만 `ResponseEntity<String>`을 반환하여 **공통 응답 포맷(ApiResponse)을 위반**함
- 나머지 6개 엔드포인트는 모두 `ApiResponse<T>`를 일관되게 사용

**실패 응답 포맷:**
- `ExceptionAdvice`에서 `GeneralException`을 catch하여 `ApiResponse.onFailure(code, message, null)` 형태로 통일 반환 → ✅
- `ConstraintViolationException`, `MethodArgumentNotValidException` 도 동일 포맷으로 처리 → ✅

#### 예상 점수: **4.0 / 5점** (saveSettings 1건 위반으로 감점)

---

### ③ 파라미터 처리 일관성 (5점)

#### 파라미터 바인딩 분석

| 엔드포인트 | 파라미터 방식 | 적합성 |
|---|---|---|
| `POST /counsels` | `@AuthenticationPrincipal Long userId` + `@RequestBody CounselReqDTO.ChatMessage` | ✅ |
| `POST /counsels/report` | `@AuthenticationPrincipal Long userId` + `@RequestBody CounselReqDTO.ChatReport` | ✅ |
| `PATCH /counsels` | `@AuthenticationPrincipal Long userId` + `@RequestBody CounselReqDTO.ChatSummary` | ✅ |
| `GET /counsels` | `@AuthenticationPrincipal Long userId` | ✅ |
| `GET /counsels/{counselId}` | `@AuthenticationPrincipal Long userId` + `@PathVariable Long counselId` | ✅ |
| `DELETE /counsels/{counselId}` | `@AuthenticationPrincipal Long userId` + `@PathVariable Long counselId` | ✅ |
| `PUT /counsels/setting` | `@RequestBody CounselorSettingReqDTO` (Body에 userId 포함) | ❌ 아래 설명 |

**✅ 양호 항목**
- `@AuthenticationPrincipal`로 인증 사용자 ID를 일관되게 추출 (6/7 엔드포인트)
- 조회(GET)에는 `@PathVariable`, 생성/수정(POST/PATCH)에는 `@RequestBody`로 일관적 구분
- record 타입 DTO(`CounselReqDTO.ChatMessage`, `CounselReqDTO.ChatSummary`)에 필요한 필드만 정의

**⚠️ 개선 필요 항목**

| # | 항목 | 상세 |
|---|------|------|
| 1 | **saveSettings의 인증 미사용** | `PUT /counsels/setting`에서 `@AuthenticationPrincipal`을 사용하지 않고 `@RequestBody`에 `userId`를 직접 받고 있음. 다른 6개 엔드포인트의 패턴과 **불일관**. 보안상 위험(타인의 userId로 설정 덮어쓰기 가능) |
| 2 | **`@Valid` 미적용** | 모든 `@RequestBody`에 `@Valid` 어노테이션 미적용. `CounselorSettingReqDTO`에는 `@NotNull`이 import되었으나 (잘못된 import), 실제 필드에 적용되지 않음 |
| 3 | **잘못된 Validation import** | `CounselorSettingReqDTO`에서 `groovyjarjarantlr4.v4.runtime.misc.NotNull` import → `jakarta.validation.constraints.NotNull`이어야 함 |
| 4 | **필수 필드 검증 부재** | `CounselReqDTO.ChatMessage`의 `text` 필드에 `@NotBlank` 미적용. 빈 문자열이나 null이 그대로 AI에 전송될 수 있음 |

#### 예상 점수: **3.0 / 5점**

---

### ④ 에러 핸들링 (5점)

#### 에러 코드 체계

**도메인 전용 에러코드 (`CounselErrorCode`):**

| 에러 코드 | HTTP 상태 | 코드 문자열 | 메시지 |
|---|---|---|---|
| `_CHATROOM_ACCESS_DENIED` | `403 FORBIDDEN` | `CHATROOM403` | 해당 채팅방에 권한이 없습니다 |
| `_CHATROOM_NOT_FOUND` | `404 NOT_FOUND` | `CHATROOM404` | 채팅방이 존재하지 않습니다 |
| `_CHATMESSAGE_NOT_FOUND` | `404 NOT_FOUND` | `CHATMESSAGE404` | 채팅이 존재하지 않습니다 |
| `_AI_SERVER_ERROR` | `408 REQUEST_TIMEOUT` | `COUNSEL408` | AI 서버와의 연결이 원활하지 않습니다 |
| `_SETTING_NOT_FOUND` | `404 NOT_FOUND` | `SETTING404` | 해당 사용자의 상담사 초기 설정이 존재하지 않습니다 |

**글로벌 에러코드 (`ErrorStatus`) 활용:**

| 에러 코드 | 사용 위치 | HTTP 상태 |
|---|---|---|
| `COUNSELOR_ALREADY_EXISTS` | `CounselorService.saveSettings()` | `409 CONFLICT` |
| `_MEMBER_NOT_FOUND` | `CounselorService.saveSettings()` | `404 NOT_FOUND` |

**✅ 양호 항목**
- 도메인별 커스텀 에러코드 enum 구현 (`CounselErrorCode implements BaseErrorCode`)
- 커스텀 예외 클래스 (`CounselException extends GeneralException`) 활용
- `ExceptionAdvice`를 통한 전역 예외 처리 → `ApiResponse.onFailure()` 통일 반환
- HTTP 상태 코드가 의미에 맞게 매핑됨 (403, 404, 408, 409 등)
- AI 외부 호출 실패 시 try-catch로 `_AI_SERVER_ERROR` 처리

**⚠️ 개선 필요 항목**

| # | 항목 | 상세 |
|---|------|------|
| 1 | **CounselorService 에러코드 불일치** | `saveSettings()`에서 `GeneralException(ErrorStatus.COUNSELOR_ALREADY_EXISTS)` 사용. 도메인 전용 `CounselErrorCode`가 아닌 글로벌 `ErrorStatus`에 상담 관련 코드가 섞여 있음 |
| 2 | **`_AI_SERVER_ERROR` 상태 코드** | `408 REQUEST_TIMEOUT` 사용 → 실제로 외부 AI 서버 오류이므로 `502 BAD_GATEWAY` 또는 `503 SERVICE_UNAVAILABLE`이 더 적합 |
| 3 | **권한 검증 누락** | `readChat()`에서는 `userId` 일치 여부 확인하지만, `askCounsel()`/`askReport()`에서 기존 chatRoom 접근 시 소유권 검증이 없음 |

#### 예상 점수: **4.0 / 5점**

---

### 📊 1-1 종합 점수 (정적 분석 기반 예상)

| 평가 항목 | 배점 | 예상 점수 | 비고 |
|---|---|---|---|
| RESTful 원칙 준수 | 5점 | **3.5점** | PATCH URL의 RPC 스타일, setting URL 설계 |
| 공통 응답 포맷 사용 | 5점 | **4.0점** | saveSettings만 ResponseEntity 사용 |
| 파라미터 처리 일관성 | 5점 | **3.0점** | @Valid 미적용, saveSettings 인증 미사용 |
| 에러 핸들링 | 5점 | **4.0점** | 에러코드 혼재, 권한 검증 일부 누락 |
| **합계** | **20점** | **14.5점** | |

---

## 1-2. API 구현 완성도 — 정적 분석 가능 범위

> ⚠️ 1-2 항목은 본래 **실제 서버 실행(Swagger 테스트)** 을 통해 평가하는 항목입니다.  
> 아래는 코드만으로 추론 가능한 부분을 정리한 것이며, 실제 점수와 차이가 있을 수 있습니다.

---

### ① 정상 동작 여부 — 코드 레벨 위험 요소 (20점)

> 아래 항목들은 런타임에서 정상 동작을 방해할 수 있는 코드 레벨 위험입니다.

| # | 위험 요소 | 위치 | 심각도 | 상세 |
|---|-----------|------|--------|------|
| 1 | **잘못된 import** | `CounselorSettingReqDTO` | 🔴 높음 | `groovyjarjarantlr4.v4.runtime.misc.NotNull` — 컴파일은 되더라도 Bean Validation으로 동작하지 않음 |
| 2 | **chatMessages empty 처리** | `askCounsel()` | 🟡 중간 | 최초 상담 시 채팅 이력이 없으면 `getRecentMessages()`가 empty list를 반환할 수 있고, `orElseThrow()`로 인해 예외 발생 가능 |
| 3 | **saveSettings 응답 비표준** | `saveSettings()` | 🟡 중간 | `ResponseEntity<String>` 반환 → 클라이언트에서 공통 응답 포맷 파싱 시 실패 가능 |
| 4 | **readChat 응답 불완전** | `EntityToChatSummary()` | 🟡 중간 | `EntityToChatSummary`에서 `userSummary`, `aiSummary`를 포함하지 않으나, `readChat()`에서 사용하는 `EntityToChatDetails()`에는 포함 → 종료 후 요약 반환 시 일부 필드 누락 가능 |
| 5 | **chatRoom.chatMessages 초기화** | `ChatRoom` entity | 🟢 낮음 | `@Builder`와 `@Builder.Default`의 조합이 없어, Builder로 생성 시 `chatMessages`가 null이 될 수 있음 (현재 `= new ArrayList<>()`지만 Builder가 덮어쓸 수 있음) |

### ② 에러 핸들링 확인 — 코드 레벨 분석 (15점)

| 시나리오 | 코드 대응 여부 | 상세 |
|---|---|---|
| 존재하지 않는 chatRoomId 전송 | ✅ 처리됨 | `chatRoomRepository.findById().orElseThrow(CounselException)` |
| 타인의 chatRoom 접근 | ⚠️ 부분 처리 | `readChat()`, `deleteChat()`에서만 userId 일치 검증. `askCounsel()`, `askReport()`, `summaryCounsel()`에서는 미검증 |
| AI 서버 장애 | ✅ 처리됨 | try-catch → `_AI_SERVER_ERROR` |
| 미인증 요청 | ✅ 처리됨 | `@AuthenticationPrincipal` (saveSettings 제외) |
| null/empty text 전송 | ❌ 미처리 | DTO에 `@NotBlank`/`@Valid` 없음 |
| 중복 상담사 설정 | ✅ 처리됨 | `counselorSettingRepository.existsById()` → `COUNSELOR_ALREADY_EXISTS` |
| 존재하지 않는 userId | ✅ 처리됨 | `userRepository.findById().orElseThrow()` |

### ③ 참고 서류 (5점)

> 📌 이 항목은 Notion, 영상, README 등 외부 문서로 평가되므로 정적 분석 범위 밖입니다.

| 확인 가능 항목 | 상태 |
|---|---|
| 기술 스택 선택 이유 문서 | ❓ README.md 확인 필요 |
| API 설계 구조 설명 문서 | ❓ Notion 등 외부 문서 확인 필요 |

---

## 🔧 발견된 주요 이슈 및 개선 권고

### 🔴 즉시 수정 권장 (감점 직결)

| # | 이슈 | 수정 방법 |
|---|------|-----------|
| 1 | `saveSettings()`가 `ResponseEntity<String>` 반환 | `ApiResponse<String>` 또는 `ApiResponse<Void>`로 변경 |
| 2 | `saveSettings()`에서 `@AuthenticationPrincipal` 미사용 | `@AuthenticationPrincipal Long userId` 추가, DTO에서 `userId` 필드 제거 |
| 3 | `CounselorSettingReqDTO`의 잘못된 import | `groovyjarjarantlr4.v4.runtime.misc.NotNull` → `jakarta.validation.constraints.NotNull`로 변경 |
| 4 | `@Valid` 전면 미적용 | 모든 `@RequestBody` 파라미터에 `@Valid` 추가 |

### 🟡 개선 권장

| # | 이슈 | 수정 방법 |
|---|------|-----------|
| 5 | PATCH 상담 종료 URL에 리소스 ID 없음 | `PATCH /api/v1/counsels/{counselId}/summary` 형태로 변경 |
| 6 | `askCounsel()`, `askReport()`에서 chatRoom 소유권 미검증 | `chatRoom.getUserId().equals(userId)` 검증 로직 추가 |
| 7 | DTO 필드에 Bean Validation 어노테이션 추가 | `CounselReqDTO.ChatMessage.text`에 `@NotBlank` 등 적용 |
| 8 | `_AI_SERVER_ERROR` HTTP 상태 코드 | `408 REQUEST_TIMEOUT` → `502 BAD_GATEWAY`로 변경 |

---

## � 교차 도메인 분석에서 발견된 추가 사항

> `application.yaml` 및 `report` 도메인 분석을 통해 발견된 counsel 도메인 관련 추가 인사이트

| # | 항목 | 상세 |
|---|------|------|
| 1 | **AI 클라이언트 이중화** | counsel 도메인은 Spring AI의 `ChatClient`를 사용하고, report 도메인은 `RestTemplate`으로 OpenAI API를 직접 호출. 동일 프로젝트에서 두 가지 AI 호출 방식이 공존하여 **아키텍처 불일관**. `application.yaml`에 Spring AI 설정(`spring.ai.openai`)과 별도 OpenAI 설정(`openai.api.key`)이 모두 존재 |
| 2 | **프롬프트 관리 이중화** | counsel은 `resources/prompts/*.st` 템플릿 파일 + `SystemPromptTemplate`을 사용하나, report는 Service 내부에서 `String.format()`으로 프롬프트 하드코딩. 일관성 부족 |
| 3 | **에러 처리 비교** | counsel은 AI 호출 실패 시 `CounselException(_AI_SERVER_ERROR)` 예외 전파 (✅). report는 에러를 삼키고 문자열 반환 (❌). counsel의 방식이 올바름 |

> 🆕 **Global 인프라 분석 (전체 도메인 공통 영향)**

| # | 항목 | Counsel 도메인 영향 |
|---|------|---|
| 4 | **`JwtAuthenticationFilter`에서 Role 미검증** | JWT에 `role` claim이 존재하나 Filter에서 빈 권한(`new ArrayList<>()`)으로 Authentication 생성. `GUEST` 사용자도 상담 API 호출 가능 — 회원가입 미완료 사용자가 AI 상담 이용 가능 |
| 5 | **`DevAuthController` 프로덕션 잔존** | `GET /api/v1/dev/login?userId=1`로 임의 토큰 발급 후 상담 API 접근 가능 — 전 도메인 공통 보안 리스크 |
| 6 | **`@Valid` 전 도메인 미적용** | counsel 도메인도 `@Valid` 미사용. `CounselReqDTO`, `CounselorSettingReqDTO`의 Bean Validation 미활용 |
| 7 | **DTO 스타일 혼재** | counsel은 `@Getter` class 방식. User 도메인은 `record` 방식. 프로젝트 전체 3종 스타일 혼재 |

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
