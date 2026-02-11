# 📋 Counsel 도메인 — API 평가 기준 정적 분석 보고서 v2

> **분석 대상**: `com.symteo.domain.counsel`  
> **분석 일시**: 2025-02-09  
> **이전 버전**: counsel-convention-v1.md (2025-02-07)  
> **분석 범위**: 평가 기준 1-1 (API 설계 완성도) + 1-2 (API 구현 완성도 — 정적 분석 가능 범위)

---

## 📌 v1 대비 주요 변경 사항 요약

| # | 이슈 (v1) | 상태 | 변경 내용 |
|---|-----------|------|-----------|
| 1 | `saveSettings()`가 `ResponseEntity<String>` 반환 | ✅ **해결** | `ApiResponse<Long>` 으로 변경 |
| 2 | `saveSettings()`에서 `@AuthenticationPrincipal` 미사용 | ✅ **해결** | `@AuthenticationPrincipal Long userId` 추가, DTO에서 `userId` 필드 제거 |
| 3 | `CounselorSettingReqDTO`의 잘못된 import (`groovyjarjarantlr4`) | ✅ **해결** | `jakarta.validation.constraints.NotNull` 로 올바르게 변경 |
| 4 | `@Valid` 전면 미적용 | ✅ **해결** | 모든 `@RequestBody` 파라미터에 `@Valid` 적용 |
| 5 | PATCH 상담 종료 URL에 리소스 ID 없음 | ✅ **해결** | `PATCH /api/v1/counsels/{counselId}/summary` 형태로 변경 |
| 6 | `_AI_SERVER_ERROR` HTTP 상태 코드가 `408` | ✅ **해결** | `503 SERVICE_UNAVAILABLE` 로 변경 |
| 7 | `CounselorService`에서 글로벌 `ErrorStatus` 사용 | ✅ **부분 해결** | `_COUNSELOR_ALREADY_EXISTS`를 도메인 에러코드로 이동. 단, `_MEMBER_NOT_FOUND`는 여전히 글로벌 `ErrorStatus` 사용 |
| 8 | DTO 필드에 Bean Validation 미적용 | ✅ **해결** | `CounselReqDTO` 전 필드에 `@NotNull`, `@NotBlank` 적용 |
| 9 | `askCounsel()`, `askReport()`에서 chatRoom 소유권 미검증 | ❌ **미해결** | 여전히 미검증 |
| 10 | `EntityToChatSummary()`에서 요약 필드 누락 | ❌ **미해결** | `userSummary`, `aiSummary` 미포함 |

---

## 1-1. API 설계 완성도 (총 20점)

---

### ① RESTful 원칙 준수 (5점)

#### 엔드포인트 목록

| HTTP 메서드 | URL 패턴 | 기능 | RESTful 적합성 |
|---|---|---|---|
| `POST` | `/api/v1/counsels` | AI 상담 요청 | ✅ 리소스 생성 의미에 부합 |
| `POST` | `/api/v1/counsels/report` | 리포트 분석 요청 | ⚠️ 아래 상세 설명 |
| `PATCH` | `/api/v1/counsels/{counselId}/summary` | 상담 종료(요약) | ✅ **v1 대비 개선** |
| `GET` | `/api/v1/counsels` | 전체 상담 조회 | ✅ 컬렉션 조회에 적합 |
| `GET` | `/api/v1/counsels/{counselId}` | 단일 상담 조회 | ✅ 단일 리소스 조회에 적합 |
| `DELETE` | `/api/v1/counsels/{counselId}` | 상담 삭제 | ✅ 리소스 삭제에 적합 |
| `PUT` | `/api/v1/counsels/setting` | 상담사 초기 설정 저장 | ⚠️ 아래 상세 설명 |

#### 상세 분석

**✅ 양호 항목**
- 기본 URL `@RequestMapping("/api/v1/counsels")`이 리소스 중심(명사, 복수형)으로 설계됨
- `GET`, `POST`, `DELETE`, `PATCH`, `PUT`을 적절히 구분하여 사용
- Path Variable(`/{counselId}`)을 통한 리소스 식별이 올바름
- API 버전 관리(`v1`)가 URL에 포함됨
- **[v1 대비 개선]** `PATCH /api/v1/counsels/{counselId}/summary` — 대상 리소스를 URL에 명시하여 RESTful 원칙에 부합하도록 개선됨

**⚠️ 개선 필요 항목**

| # | 항목 | 현재 상태 | 권장 사항 | 감점 요인 |
|---|------|-----------|-----------|-----------|
| 1 | 리포트 분석 URL | `POST /api/v1/counsels/report` | `/api/v1/counsels/{counselId}/report` 처럼 리소스 계층 구조를 더 명확히 하는 것이 좋음 | 경미 |
| 2 | 설정 저장 URL | `PUT /api/v1/counsels/setting` | 상담사 설정은 상담 리소스와 별개. `/api/v1/counselor-settings`가 더 RESTful | 경미 |

#### 예상 점수: **4.5 / 5점** (v1: 3.5점 → +1.0점 상승)

> **점수 상승 근거**: PATCH 엔드포인트의 RPC 스타일 문제가 완전 해결됨. 남은 이슈는 경미한 URL 네이밍 수준.

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
| `PATCH /counsels/{counselId}/summary` | `ApiResponse<CounselResDTO.ChatSummary>` | ✅ |
| `GET /counsels` | `ApiResponse<List<CounselResDTO.Chat>>` | ✅ |
| `GET /counsels/{counselId}` | `ApiResponse<CounselResDTO.ChatSummary>` | ✅ |
| `DELETE /counsels/{counselId}` | `ApiResponse<Long>` | ✅ |
| `PUT /counsels/setting` | `ApiResponse<Long>` | ✅ **v1 대비 개선** |

**✅ 핵심 개선:**
- **[v1 대비 개선]** `saveSettings()`가 `ResponseEntity<String>` → `ApiResponse<Long>`으로 변경되어 **7개 엔드포인트 전부 공통 응답 포맷 준수**

**실패 응답 포맷:**
- `ExceptionAdvice`에서 `GeneralException`을 catch하여 `ApiResponse.onFailure(code, message, null)` 형태로 통일 반환 → ✅
- `ConstraintViolationException`, `MethodArgumentNotValidException`도 동일 포맷으로 처리 → ✅

#### 예상 점수: **5.0 / 5점** (v1: 4.0점 → +1.0점 상승)

> **점수 상승 근거**: 유일한 위반이었던 `saveSettings()`의 `ResponseEntity<String>` 반환이 `ApiResponse<Long>`으로 수정되어 전 엔드포인트 완전 일관.

---

### ③ 파라미터 처리 일관성 (5점)

#### 파라미터 바인딩 분석

| 엔드포인트 | 파라미터 방식 | 적합성 |
|---|---|---|
| `POST /counsels` | `@AuthenticationPrincipal Long userId` + `@RequestBody @Valid CounselReqDTO.ChatMessage` | ✅ |
| `POST /counsels/report` | `@AuthenticationPrincipal Long userId` + `@RequestBody @Valid CounselReqDTO.ChatReport` | ✅ |
| `PATCH /counsels/{counselId}/summary` | `@AuthenticationPrincipal Long userId` + `@PathVariable Long counselId` | ✅ |
| `GET /counsels` | `@AuthenticationPrincipal Long userId` | ✅ |
| `GET /counsels/{counselId}` | `@AuthenticationPrincipal Long userId` + `@PathVariable Long counselId` | ✅ |
| `DELETE /counsels/{counselId}` | `@AuthenticationPrincipal Long userId` + `@PathVariable Long counselId` | ✅ |
| `PUT /counsels/setting` | `@AuthenticationPrincipal Long userId` + `@RequestBody @Valid CounselorSettingReqDTO.CounselorSetting` | ✅ **v1 대비 개선** |

**✅ 양호 항목**
- **[v1 대비 개선]** `@AuthenticationPrincipal`로 인증 사용자 ID를 **7/7 엔드포인트** 전부 일관되게 추출 (v1: 6/7)
- **[v1 대비 개선]** `@Valid`가 모든 `@RequestBody` 파라미터에 적용됨
- **[v1 대비 개선]** `CounselorSettingReqDTO`에 `jakarta.validation.constraints.NotNull` 올바르게 적용
- **[v1 대비 개선]** `CounselReqDTO.ChatMessage.text`에 `@NotBlank` 적용
- 조회(GET)에는 `@PathVariable`, 생성/수정(POST/PATCH)에는 `@RequestBody`로 일관적 구분
- record 타입 DTO에 필요한 필드만 정의

**⚠️ 잔존 이슈**

| # | 항목 | 상세 |
|---|------|------|
| 1 | `CounselReqDTO.ChatMessage.chatRoomId`에 `@NotNull` 적용 | 최초 상담 시 `chatRoomId`가 `null`이어야 새 채팅방이 생성되는 로직인데, `@NotNull`로 인해 최초 상담 요청이 Validation에서 거부됨. `chatRoomId`는 nullable이어야 함 |

#### 예상 점수: **4.0 / 5점** (v1: 3.0점 → +1.0점 상승)

> **점수 상승 근거**: `@Valid` 전면 적용, `@AuthenticationPrincipal` 완전 일관성, 올바른 import. 단, `chatRoomId`의 `@NotNull`이 비즈니스 로직과 충돌하여 감점 요소 잔존.

---

### ④ 에러 핸들링 (5점)

#### 에러 코드 체계

**도메인 전용 에러코드 (`CounselErrorCode`):**

| 에러 코드 | HTTP 상태 | 코드 문자열 | 메시지 |
|---|---|---|---|
| `_CHATROOM_ACCESS_DENIED` | `403 FORBIDDEN` | `CHATROOM403` | 해당 채팅방에 권한이 없습니다 |
| `_CHATROOM_NOT_FOUND` | `404 NOT_FOUND` | `CHATROOM404` | 채팅방이 존재하지 않습니다 |
| `_CHATMESSAGE_NOT_FOUND` | `404 NOT_FOUND` | `CHATMESSAGE404` | 채팅이 존재하지 않습니다 |
| `_AI_SERVER_ERROR` | `503 SERVICE_UNAVAILABLE` | `COUNSEL503` | AI 서버와의 연결이 원활하지 않습니다 |
| `_COUNSELOR_ALREADY_EXISTS` | `409 CONFLICT` | `COUNSELOR409` | 이미 상담사 설정이 존재합니다 |
| `_COUNSELOR_NOT_FOUND` | `404 NOT_FOUND` | `COUNSELOR404` | 상담사 설정을 찾을 수 없습니다 |
| `_SETTING_SAVE_ERROR` | `500 INTERNAL_SERVER_ERROR` | `SETTING500` | 상담사 설정 저장 요청이 실패했습니다 |
| `_SETTING_NOT_FOUND` | `404 NOT_FOUND` | `SETTING404` | 해당 사용자의 상담사 초기 설정이 존재하지 않습니다 |

**✅ 양호 항목**
- 도메인별 커스텀 에러코드 enum 구현 (`CounselErrorCode implements BaseErrorCode`)
- 커스텀 예외 클래스 (`CounselException extends GeneralException`) 활용
- `ExceptionAdvice`를 통한 전역 예외 처리 → `ApiResponse.onFailure()` 통일 반환
- HTTP 상태 코드가 의미에 맞게 매핑됨 (403, 404, 409, 500, 503 등)
- AI 외부 호출 실패 시 try-catch로 `_AI_SERVER_ERROR` 처리
- **[v1 대비 개선]** `_AI_SERVER_ERROR`가 `408 REQUEST_TIMEOUT` → `503 SERVICE_UNAVAILABLE`로 올바르게 변경
- **[v1 대비 개선]** `_COUNSELOR_ALREADY_EXISTS`가 글로벌 `ErrorStatus`에서 도메인 `CounselErrorCode`로 이동
- **[v1 대비 개선]** `_SETTING_SAVE_ERROR(500)` 추가 — DB 저장 실패 시 전용 에러코드로 처리
- **[v1 대비 개선]** `saveSettings()`에서 `CounselException(CounselErrorCode._COUNSELOR_ALREADY_EXISTS)` 사용하여 도메인 에러코드 일관성 확보

**⚠️ 잔존 이슈**

| # | 항목 | 상세 |
|---|------|------|
| 1 | **`PromptFactoryImpl`에서 글로벌 에러코드 사용** | `createSBPrompt()`, `createDAPrompt()`, `createAPrompt()`에서 유저 조회 실패 시 `GeneralException(ErrorStatus._MEMBER_NOT_FOUND)` 사용. 도메인 전용 `CounselErrorCode`가 아닌 글로벌 에러코드 사용 |
| 2 | **권한 검증 누락** | `askCounsel()`, `askReport()`에서 기존 chatRoom 접근 시 소유권 검증이 없음. `readChat()`, `deleteChat()`에서만 userId 일치 검증 |

#### 예상 점수: **4.5 / 5점** (v1: 4.0점 → +0.5점 상승)

> **점수 상승 근거**: AI 에러 상태 코드 정정, 에러코드 도메인 통일, 저장 실패 에러코드 추가. 권한 검증 누락과 `PromptFactoryImpl` 글로벌 에러코드 혼재로 소폭 감점.

---

### 📊 1-1 종합 점수 (정적 분석 기반 예상)

| 평가 항목 | 배점 | v1 점수 | v2 점수 | 변동 | 비고 |
|---|---|---|---|---|---|
| RESTful 원칙 준수 | 5점 | 3.5점 | **4.5점** | +1.0 | PATCH URL 개선 완료 |
| 공통 응답 포맷 사용 | 5점 | 4.0점 | **5.0점** | +1.0 | 전 엔드포인트 ApiResponse 통일 |
| 파라미터 처리 일관성 | 5점 | 3.0점 | **4.0점** | +1.0 | @Valid, @AuthenticationPrincipal 완전 일관 |
| 에러 핸들링 | 5점 | 4.0점 | **4.5점** | +0.5 | 에러코드 도메인 통일, AI 상태코드 정정 |
| **합계** | **20점** | **14.5점** | **18.0점** | **+3.5** | |

---

## 1-2. API 구현 완성도 — 정적 분석 가능 범위

> ⚠️ 1-2 항목은 본래 **실제 서버 실행(Swagger 테스트)** 을 통해 평가하는 항목입니다.  
> 아래는 코드만으로 추론 가능한 부분을 정리한 것이며, 실제 점수와 차이가 있을 수 있습니다.

---

### ① 정상 동작 여부 — 코드 레벨 위험 요소 (20점)

| # | 위험 요소 | 위치 | 심각도 | 상태 | 상세 |
|---|-----------|------|--------|------|------|
| 1 | ~~잘못된 import (`groovyjarjarantlr4`)~~ | `CounselorSettingReqDTO` | — | ✅ **해결** | `jakarta.validation.constraints.NotNull`로 올바르게 변경 |
| 2 | `chatRoomId`에 `@NotNull`과 로직 충돌 | `CounselReqDTO.ChatMessage` | 🔴 높음 | 🆕 **신규** | 최초 상담 시 `chatRoomId`가 `null`이어야 하지만, `@NotNull` 검증으로 인해 400 에러 발생. Validation과 비즈니스 로직 불일치 |
| 3 | `chatMessages` empty list 처리 | `askCounsel()` | 🟡 중간 | ❌ **미해결** | 최초 상담 시 이전 채팅 이력이 없으면 `getRecentMessages()`가 empty list를 반환할 수 있고, `orElseThrow()`로 인해 예외 발생 가능 |
| 4 | ~~`saveSettings` 응답 비표준~~ | `saveSettings()` | — | ✅ **해결** | `ApiResponse<Long>` 반환으로 변경 |
| 5 | `EntityToChatSummary()` 응답 불완전 | `CounselConverter` | 🟡 중간 | ❌ **미해결** | `summaryCounsel()` 메서드에서 사용하는 `EntityToChatSummary()`에 `userSummary`, `aiSummary` 미포함. 요약 후 반환 시 해당 필드가 null |
| 6 | `ChatRoom.chatMessages` Builder 초기화 | `ChatRoom` entity | 🟢 낮음 | ❌ **미해결** | `@Builder`와 `@Builder.Default` 없이 `= new ArrayList<>()`만 선언. Builder로 생성 시 `chatMessages`가 null이 될 수 있음 |
| 7 | `ChatMessageRepository` 잘못된 import | `ChatMessageRepository` | 🟢 낮음 | 🆕 **신규** | `import java.awt.print.Pageable` — 사용하지 않는 잘못된 `Pageable` import 존재. 실제 파라미터는 `PageRequest`이므로 동작에는 영향 없으나, 코드 정리 필요 |
| 8 | `askReport()` `@Transactional` 누락 | `askReport()` | 🟡 중간 | 🆕 **신규** | `askCounsel()`은 `@Transactional` 적용되어 있으나, `askReport()`에는 미적용. DB 저장(`chatMessageRepository.save()`)이 포함되어 있어 트랜잭션 관리 필요 |

### ② 에러 핸들링 확인 — 코드 레벨 분석 (15점)

| 시나리오 | 코드 대응 여부 | v1 대비 변화 | 상세 |
|---|---|---|---|
| 존재하지 않는 chatRoomId 전송 | ✅ 처리됨 | 유지 | `chatRoomRepository.findById().orElseThrow(CounselException)` |
| 타인의 chatRoom 접근 | ⚠️ 부분 처리 | **미해결** | `readChat()`, `deleteChat()`에서만 userId 일치 검증. `askCounsel()`, `askReport()`, `summaryCounsel()`에서는 미검증 |
| AI 서버 장애 | ✅ 처리됨 | 유지 | try-catch → `_AI_SERVER_ERROR` (503) |
| 미인증 요청 | ✅ 처리됨 | **개선** | 전 엔드포인트 `@AuthenticationPrincipal` 적용 (v1: 6/7 → v2: 7/7) |
| null/empty text 전송 | ✅ 처리됨 | **개선** | `@NotBlank` + `@Valid` 적용 |
| 중복 상담사 설정 | ✅ 처리됨 | **개선** | 도메인 전용 `CounselErrorCode._COUNSELOR_ALREADY_EXISTS` 사용 |
| 존재하지 않는 userId | ✅ 처리됨 | 유지 | `userRepository.findById().orElseThrow()` |
| 상담사 설정 DB 저장 실패 | ✅ 처리됨 | **신규** | try-catch → `_SETTING_SAVE_ERROR` (500) |

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
| 1 | `CounselReqDTO.ChatMessage.chatRoomId`의 `@NotNull`이 비즈니스 로직과 충돌 | `@NotNull` 제거하여 nullable 허용. 최초 상담 시 `null`이면 새 채팅방 생성 로직이 정상 작동하도록 변경 | `CounselReqDTO.java` |
| 2 | `askCounsel()`, `askReport()`, `summaryCounsel()`에서 chatRoom 소유권 미검증 | 기존 chatRoom 조회 후 `chatRoom.getUserId().equals(userId)` 검증 로직 추가 | `CounselCommandServiceImpl.java` |
| 3 | `EntityToChatSummary()`에서 `userSummary`, `aiSummary` 누락 | `.userSummary(chatRoom.getUserSummary())`, `.aiSummary(chatRoom.getAiSummary())` 추가 | `CounselConverter.java` |

### 🟡 개선 권장

| # | 이슈 | 수정 방법 | 관련 파일 |
|---|------|-----------|-----------|
| 4 | `getRecentMessages()` 최초 상담 시 empty list로 인한 예외 가능 | `Optional<List>` 대신 빈 리스트 반환(`List<ChatMessage>`)으로 변경하거나, empty일 때 `orElseThrow()` 대신 `orElse(List.of())` 사용 | `CounselCommandServiceImpl.java`, `ChatMessageRepository.java` |
| 5 | `askReport()`에 `@Transactional` 미적용 | `@Transactional` 어노테이션 추가 | `CounselCommandServiceImpl.java` |
| 6 | `PromptFactoryImpl`에서 글로벌 `ErrorStatus._MEMBER_NOT_FOUND` 사용 | 도메인 전용 에러코드 사용 또는 의도적 글로벌 사용이라면 주석으로 명시 | `PromptFactoryImpl.java` |
| 7 | `ChatRoom` entity에 `@Builder.Default` 누락 | `@Builder.Default private List<ChatMessage> chatMessages = new ArrayList<>();` 형태로 변경 | `ChatRoom.java` |
| 8 | `ChatMessageRepository`에 미사용 잘못된 import | `import java.awt.print.Pageable;` 제거 | `ChatMessageRepository.java` |
| 9 | Enum 네이밍 컨벤션 | `Answer_Format`, `Support_Style`, `Counselor_Role` → Java 표준 PascalCase(`AnswerFormat`, `SupportStyle`, `CounselorRole`)로 변경 권장 | `enums/` 하위 파일들 |

### 🟢 코드 품질 권장

| # | 이슈 | 수정 방법 |
|---|------|-----------|
| 10 | `readAllChat()`에 불필요한 `@Transactional` | 읽기 전용 쿼리에 `@Transactional(readOnly = true)` 사용 또는 제거 |
| 11 | 리포트 분석 URL 개선 | `POST /api/v1/counsels/{counselId}/report` 형태로 리소스 계층 명확화 |
| 12 | 설정 URL 분리 고려 | 상담사 설정을 별도 리소스로 분리: `/api/v1/counselor-settings` |

---

## 📊 v1 → v2 점수 변동 총괄

### 1-1. API 설계 완성도

| 평가 항목 | 배점 | v1 점수 | v2 점수 | 변동 |
|---|---|---|---|---|
| RESTful 원칙 준수 | 5점 | 3.5점 | **4.5점** | ▲ +1.0 |
| 공통 응답 포맷 사용 | 5점 | 4.0점 | **5.0점** | ▲ +1.0 |
| 파라미터 처리 일관성 | 5점 | 3.0점 | **4.0점** | ▲ +1.0 |
| 에러 핸들링 | 5점 | 4.0점 | **4.5점** | ▲ +0.5 |
| **합계** | **20점** | **14.5점** | **18.0점** | **▲ +3.5** |

### 1-2. API 구현 완성도 (정적 분석 가능 범위)

| 평가 항목 | 이슈 수(v1) | 이슈 수(v2) | 변동 |
|---|---|---|---|
| 코드 레벨 위험 요소 | 5건 | 6건 (2건 해결, 3건 신규 발견) | — |
| 에러 핸들링 미처리 시나리오 | 2건 | 1건 | ▲ 개선 |

---

## 📊 예상 도메인 간 비교 (v2 기준)

| 평가 항목 | Counsel (v2) | Diagnose | Home | Report | TodayMission | User |
|---|---|---|---|---|---|---|
| RESTful 원칙 준수 | **4.5 / 5** | 5.0 / 5 | 3.5 / 5 | 3.5 / 5 | 2.5 / 5 | 3.5 / 5 |
| 공통 응답 포맷 사용 | **5.0 / 5** | 5.0 / 5 | 4.5 / 5 | 5.0 / 5 | 5.0 / 5 | 4.0 / 5 |
| 파라미터 처리 일관성 | **4.0 / 5** | 3.5 / 5 | 3.0 / 5 | 4.5 / 5 | 3.0 / 5 | 3.5 / 5 |
| 에러 핸들링 | **4.5 / 5** | 4.0 / 5 | 3.0 / 5 | 3.0 / 5 | 3.0 / 5 | 3.5 / 5 |
| **합계** | **18.0 / 20** | 17.5 / 20 | 14.0 / 20 | 16.0 / 20 | 13.5 / 20 | 14.5 / 20 |

> 📈 **Counsel 도메인이 v2 기준으로 전 도메인 중 1위 (18.0점)** 로 상승. 이전 v1에서는 Diagnose(17.5)에 이어 3위 공동이었음.

---

## 📝 비고

- 이 보고서는 **소스 코드 정적 분석**만을 기반으로 작성되었습니다.
- v1에서 지적된 **8개 주요 이슈 중 7개가 해결**되었으며, 권한 검증 누락 1건이 미해결 상태입니다.
- 새로 발견된 이슈(`chatRoomId @NotNull` 충돌, `askReport() @Transactional` 누락 등)는 v1 시점에 코드가 달랐기 때문에 발생한 신규 이슈입니다.
- 1-2 항목의 **정상 동작 여부**(20점)와 **에러 핸들링 확인**(15점)은 실제 Swagger 테스트 또는 통합 테스트를 통해 최종 평가되어야 합니다.
- 예상 점수는 평가자의 기준에 따라 달라질 수 있으며, 참고용으로만 활용하시기 바랍니다.
