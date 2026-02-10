# 🔍 Symteo-BE 코드 컨벤션 검사 리포트

## 📋 검사 기준

README.md 및 code-convention.md에 명시된 규칙을 기준으로 검사합니다.

- **들여쓰기**: Space 2칸
- **문자열**: Double Quote(`" "`) 사용
- **커밋 메시지**: `타입: 내용 (#이슈번호)`
- **브랜치 전략**: `이름/이슈번호/기능명`
- **프로젝트 구조**: SOA + MVC 패턴
- **패키지 구조**: config / controller / service / domain(entity) / repository / dto / global

---

## 🏗️ Domain 디렉토리 검사

---

### 1. `domain/counsel` (AI 맞춤 상담)

#### ✅ 양호한 사항
- DTO가 `dto/req`, `dto/res`로 분리되어 있어 구조적으로 깔끔함
- Converter 클래스(`CounselConverter`, `CounselMessageConverter`)를 통해 엔티티 ↔ DTO 변환 로직 분리
- Service가 Command/Query로 분리됨 (`CounselCommandServiceImpl`, `CounselQueryServiceImpl`)
- 커스텀 예외(`CounselException`, `CounselErrorCode`)가 도메인 내부에 존재

#### ⚠️ 부족한 사항

| 항목 | 상세 내용 |
|------|-----------|
| **Enum 네이밍** | `Support_Style`, `Counselor_Role`, `Answer_Format` 등이 **snake_case**를 사용 중. Java 컨벤션상 `SupportStyle`, `CounselorRole`, `AnswerFormat` (PascalCase)이어야 함 |
| **Enum 값 네이밍** | `Tone.formal`, `Tone.unformal` 등 enum 상수값이 **소문자**로 작성됨. Java 컨벤션상 `FORMAL`, `UNFORMAL` (UPPER_SNAKE_CASE)이어야 함 |
| **Enum 값 네이밍** | `Support_Style.empathic`, `Answer_Format.situational` 등 모든 enum 상수가 소문자 |
| **DTO 네이밍** | `CounselReqDTO`, `CounselResDTO`에서 `DTO` 접미사가 대문자로 표기 — 프로젝트 내 일관성 확인 필요 (`Request`/`Response` 접미사와 혼용) |
| **CounselReqDTO import** | `CounselorSettingReqDTO`에서 `groovyjarjarantlr4.v4.runtime.misc.NotNull`을 import — 올바르지 않은 라이브러리. `jakarta.validation.constraints.NotNull`을 사용해야 함 |
| **메서드 네이밍** | `CounselConverter.EntityToChatDetails()` — 메서드명이 대문자로 시작. Java 컨벤션상 `entityToChatDetails()`여야 함 |
| **문자열 포맷팅** | `CounselCommandServiceImpl`의 상담 요약 프롬프트에서 raw string `\s` 사용 — 가독성 및 호환성 확인 필요 |

---

### 2. `domain/diagnose` (심리 진단 테스트)

#### ✅ 양호한 사항
- Entity에 JPA 어노테이션이 적절히 사용됨
- 커스텀 에러코드(`DiagnoseErrorCode`)가 도메인 내부에 존재

#### ⚠️ 부족한 사항

| 항목 | 상세 내용 |
|------|-----------|
| **Entity에 @Setter** | `Diagnose` 엔티티에 `@Setter`가 사용됨. JPA 엔티티에서 `@Setter`는 불변성을 해침 — 별도 update 메서드를 통해 상태 변경하는 것이 권장됨 |
| **@RequiredArgsConstructor + @AllArgsConstructor** | `Diagnose` 엔티티에 두 생성자 어노테이션이 공존 — `@Builder`와 `@NoArgsConstructor(access = PROTECTED)` 조합이 더 적절 |
| **Controller 부재** | 진단 관련 Controller 파일이 워크스페이스에서 확인되지 않음 (다른 도메인에서 처리 가능하나, 독립적 엔드포인트가 필요한지 확인 필요) |

---

### 3. `domain/report` (진단 리포트)

#### ✅ 양호한 사항
- 리포트 유형별 서비스 분리 (`DepressionAnxietyReportsService`, `StressReportsService`, `AttachmentReportsService`)
- AI 모델 서비스 인터페이스(`AiModelService`) + 구현체(`AiModelServiceImpl`) 분리
- DTO가 내부 static class로 체계적으로 구조화됨 (`ReportsResponse`)
- `@Transactional(readOnly = true)` 적절히 사용

#### ⚠️ 부족한 사항

| 항목 | 상세 내용 |
|------|-----------|
| **DTO 파일 비대화** | `ReportsResponse.java` 하나에 10개 이상의 inner static class 존재 (`CreateReportResult`, `DepressionAnxietyReportDetail`, `OverallSummary`, `PhqDetail`, `GadDetail`, `ClusterResult`, `AiInsightCard`, `IntegratedReportDetail`, `StressDetail`, `BurnoutDetail`, `AttachmentReportDetail`, `AttachmentScore`, `AttachmentCard`). 리포트 유형별로 DTO 파일을 분리하는 것을 권장 |
| **매직 넘버 사용** | `DepressionAnxietyReportsService`에서 `score >= 20`, `>= 15`, `>= 10` 등 하드코딩된 숫자 기준. 상수로 추출 권장 |
| **매직 넘버 사용** | `AttachmentReportsService`에서 `anxiety < 3.0`, `avoidance < 3.0`, `score >= 4.0` 등 하드코딩 |
| **매직 넘버 사용** | `StressReportsService`에서 `ip < 20`, `ep > 80`, `ip > 70`, `op > 70` 등 하드코딩 |
| **색상 하드코딩** | `getColorByRatio()`에서 `"#F4574F"`, `"#FFAC79"` 등 색상 코드가 직접 문자열로 하드코딩 — 상수 또는 enum으로 관리 권장 |
| **주석 품질** | `StressReportsService`에서 `// 데이터 비교 케이스 // if (현재 통제감 < 지난 달 통제감) 로직은 추후 히스토리 기능 구현 시 적용 가능` — TODO 주석으로 변환 권장 |
| **맞춤법 오류** | `generateDetailedInsights()`에서 `"스스로"` → `"스스로"` 2회 등장 (올바른 표기: `"스스로"` 혹은 `"스스로에"` → 실제로는 `"스스로"` 자체가 `"스스로"` 그대로일 수 있으나, `"스스로"` vs `"스스로"` 확인 필요) |
| **Collector 사용** | `Collectors.toList()` 대신 Java 16+ `.toList()` 사용 가능 (Java 17 프로젝트이므로) |

---

### 4. `domain/todayMission` (오늘의 미션)

#### ✅ 양호한 사항
- Entity 구조가 명확 (`Missions`, `UserMissions`, `Drafts`, `MissionImages`)
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)` 적용 (JPA 엔티티 모범 사례)
- DTO가 역할별로 분리됨 (`MissionResponse`, `DraftSaveRequest`, `DraftSaveResponse`, `UserMissionStartRequest`)
- `@Scheduled`를 활용한 일일 미션 자동 할당 구현

#### ⚠️ 부족한 사항

| 항목 | 상세 내용 |
|------|-----------|
| **Service 단일 파일** | `MissionService.java` 하나에 미션 생성, 조회, 시작, 임시저장, 완료, 새로고침 등 모든 로직이 집중됨 — Command/Query 분리 또는 기능별 분리 권장 (counsel 도메인처럼) |
| **Controller 파라미터** | `MissionController`에서 `@RequestPart(required = false) String contents` — `@RequestBody` 또는 별도 DTO로 바인딩하는 것이 더 명확 |
| **DTO 일관성** | `UserMissionStartRequest`에 `imageUrl` 필드가 있으나, 실제 Controller에서는 `MultipartFile image`를 받고 있음 — `imageUrl` 필드가 불필요하거나 혼란 유발 |
| **미사용 DTO 필드** | `DraftSaveRequest`가 존재하나 Controller에서 직접 `String contents`를 `@RequestBody`로 받는 구조 — DTO 활용 일관성 부족 |
| **매직 넘버** | `MissionDataInitializer`에서 `LocalDateTime.of(9999, 12, 31, 23, 59)` — 상수로 추출 권장 |
| **변수명 가독성** | `determineCategory()`에서 `long n`, `long r` 같은 단일 문자 변수명 — 의미 있는 이름 권장 (`dayCount`, `remainder` 등) |

---

### 5. `domain/user` (사용자/MY 심터)

#### ✅ 양호한 사항
- `@Transactional(readOnly = true)` 클래스 레벨 적용, 쓰기 메서드에만 `@Transactional` 오버라이드
- DTO에 `record` 타입 적극 활용 (`MissionHistoryResponse`, `UserSettingsResponse`, `CounselorSettingsResponse`)
- enum `Role`에 적절한 설명 주석

#### ⚠️ 부족한 사항

| 항목 | 상세 내용 |
|------|-----------|
| **Service 비대화** | `UserService.java` 하나에 닉네임 중복 확인, 상담사 설정 수정, 미션 이력 조회, 미션 상세 조회, 미션 수정 등 다양한 기능 집중 — facade 패턴 또는 기능별 서비스 분리 권장 |
| **잘못된 import** | `UserService`에서 `com.sun.jdi.request.DuplicateRequestException` import — JDK 내부 패키지 사용은 지양해야 함. 커스텀 예외로 대체 필요 |
| **Controller 경로** | `UserController`의 `@RequestMapping("/api/v1/users/")` — 끝에 슬래시(`/`)가 포함됨. `/api/v1/users`로 통일 권장 |
| **DTO 네이밍 불일관** | `UpdateMissionRequest`(클래스) vs `MissionHistoryResponse`(record) — 같은 도메인 내에서 일반 클래스와 record가 혼용됨 |
| **DTO 네이밍 불일관** | `CounselorSettingsResponse`는 record, `UpdateMissionRequest`는 일반 @Getter 클래스 — 통일 필요 |
| **app.version 기본값** | `@Value("${app.version:0.0.1}")` — 하드코딩된 기본값. 빌드 타임에 자동 주입되도록 개선 권장 |

---

### 6. `domain/home` (홈 화면)

#### ✅ 양호한 사항
- Entity (`TodayLines`)가 간결하고 명확
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)` 적용

#### ⚠️ 부족한 사항

| 항목 | 상세 내용 |
|------|-----------|
| **Entity timestamp** | `TodayLines`에서 `createdAt`을 `@CreationTimestamp` 대신 `LocalDateTime.now()`로 직접 설정 — JPA 표준 `@CreationTimestamp` 또는 `@PrePersist` 사용 권장 |
| **Controller/DTO 부재** | `HomeService`만 확인 가능하고, 관련 Controller나 DTO 구조가 워크스페이스에서 확인되지 않음 |

---

## 🌐 Global 디렉토리 검사

---

### `global/ApiPayload`

#### ⚠️ 부족한 사항

| 항목 | 상세 내용 |
|------|-----------|
| **패키지 네이밍** | `ApiPayload` — Java 패키지명은 소문자로 작성하는 것이 컨벤션 (`apipayload` 또는 `api_payload`). 현재 PascalCase 사용 |
| **ApiResponse import** | `ApiResponse`에서 `CounselResDTO`를 직접 import — 글로벌 공통 모듈이 특정 도메인에 의존하면 안 됨 (결합도 증가) |
| **ExceptionAdvice** | `ExceptionAdvice`에서 `@RestControllerAdvice(annotations = {RestController.class})` — `@Controller` 기반 예외도 처리해야 할 수 있음 |

---

### `global/config`

#### ⚠️ 부족한 사항

| 항목 | 상세 내용 |
|------|-----------|
| **Data Initializer 다수** | `DataInitializer`, `MissionDataInitializer`, `TodayLineDataInitializer` 3개의 초기화 클래스 존재 — 하나의 통합 초기화 클래스 또는 SQL 기반 `data.sql` 마이그레이션으로 관리하는 것이 유지보수에 유리 |
| **MissionDataInitializer 비대화** | 약 180줄 이상의 하드코딩된 미션 문자열 — DB seed 파일(CSV/SQL)로 분리 권장 |
| **TodayLineDataInitializer 비대화** | 약 100줄 이상의 하드코딩된 응원 문구 — 동일하게 외부 파일로 분리 권장 |
| **SwaggerConfig 주석** | `// DevAuthController 관련 내용이므로` — 의미가 불명확하며 오해의 소지가 있음 |

---

### `global/auth`

#### ⚠️ 부족한 사항

| 항목 | 상세 내용 |
|------|-----------|
| **RefreshTokenRequest** | `@NoArgsConstructor`만 있고 `@AllArgsConstructor`가 없음 — 테스트/직렬화 시 생성 편의성 부족 |
| **유효성 검증 부재** | `RefreshTokenRequest`의 `refreshToken` 필드에 `@NotBlank` 등 검증 어노테이션 미적용 |

---

## 📊 전체 요약

### 공통적으로 발견된 문제 패턴

| # | 패턴 | 해당 위치 | 심각도 |
|---|------|-----------|--------|
| 1 | **Enum 네이밍 위반** (snake_case, 소문자 상수) | `counsel/enums/*` | 🔴 높음 |
| 2 | **매직 넘버 하드코딩** | `report/service/*`, `config/*` | 🟡 중간 |
| 3 | **Service 비대화** (SRP 위반) | `user/service`, `todayMission/service` | 🟡 중간 |
| 4 | **패키지명 대소문자** | `global/ApiPayload` | 🟡 중간 |
| 5 | **글로벌 모듈의 도메인 의존** | `ApiResponse` → `CounselResDTO` | 🔴 높음 |
| 6 | **잘못된 라이브러리 import** | `UserService`(sun.jdi), `CounselorSettingReqDTO`(groovy) | 🔴 높음 |
| 7 | **초기화 데이터 하드코딩** | `config/*DataInitializer` | 🟢 낮음 |
| 8 | **DTO 스타일 혼용** (class vs record) | `user/dto/*` | 🟢 낮음 |
| 9 | **Entity @Setter 사용** | `diagnose/entity/Diagnose` | 🟡 중간 |
| 10 | **메서드명 대문자 시작** | `CounselConverter.EntityToChatDetails()` | 🟡 중간 |

---

### 도메인별 컨벤션 준수율 (체감 평가)

| 도메인 | 구조 | 네이밍 | DTO 일관성 | 예외 처리 | 종합 |
|--------|------|--------|------------|-----------|------|
| counsel | ⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ |
| diagnose | ⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ |
| report | ⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ |
| todayMission | ⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ |
| user | ⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ |
| home | ⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ |

> ⭐ 1~2: 개선 필요 | ⭐ 3: 보통 | ⭐ 4~5: 양호