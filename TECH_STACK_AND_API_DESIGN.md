# 🏗️ Symteo (심터) - 기술 스택 선택 이유 & API 설계 구조

> **작성일**: 2026년 2월 12일  
> **작성자**: 심터 백엔드 팀

---

## 📌 목차

1. [기술 스택 선택 이유](#1-기술-스택-선택-이유)
2. [API 설계 목적 및 구조](#2-api-설계-목적-및-구조)
3. [도메인별 API 상세 설명](#3-도메인별-api-상세-설명)
4. [아키텍처 설계 원칙](#4-아키텍처-설계-원칙)

---

## 1. 기술 스택 선택 이유

### 1.1 Framework & Language

#### ✅ Spring Boot 3.4.1 + Java 17

**선택 이유:**
- **안정성 & 생태계**: 국내외에서 가장 많이 사용되는 엔터프라이즈급 프레임워크
- **보안**: Spring Security를 통한 강력한 인증/인가 시스템 구축 가능
- **확장성**: 멘탈 케어 서비스의 향후 확장(AI 고도화, 대규모 트래픽 처리)을 고려한 선택
- **Java 17**: LTS 버전으로 안정적이며, Record, Pattern Matching 등 최신 기능 활용 가능

#### ✅ Gradle

**선택 이유:**
- Maven 대비 빠른 빌드 속도
- Groovy DSL로 가독성 높은 설정 파일 작성 가능
- 멀티 모듈 프로젝트 확장 시 유연한 대응 가능

---

### 1.2 Database & ORM

#### ✅ MySQL 8.0 + Spring Data JPA

**선택 이유:**
- **MySQL**: 
  - 오픈소스이며 안정적인 관계형 데이터베이스
  - 사용자 정보, 미션 기록, 상담 내역 등 **정형 데이터**가 많은 심터 서비스에 적합
  - 트랜잭션 처리가 중요한 금융/의료 도메인에서도 검증된 안정성
  
- **Spring Data JPA**:
  - 반복적인 CRUD 코드 최소화
  - Entity 기반 객체지향 설계로 유지보수성 향상
  - QueryDSL, Specification과 결합하여 복잡한 쿼리 처리 가능

#### ✅ Flyway

**선택 이유:**
- **데이터베이스 버전 관리**: SQL 마이그레이션 파일로 스키마 변경 이력 추적
- **팀 협업**: 여러 개발자가 동시에 작업해도 DB 스키마 충돌 방지
- **배포 안정성**: 로컬/개발/운영 환경에서 일관된 스키마 유지

#### ✅ Redis

**선택 이유:**
- **JWT Refresh Token 저장**: 로그아웃 시 토큰 무효화 관리
- **세션 관리**: Stateless 환경에서 빠른 인증 상태 확인
- **캐싱**: 자주 조회되는 데이터(홈 화면 대시보드, 미션 정보 등) 캐싱으로 응답 속도 향상

---

### 1.3 Security & Authentication

#### ✅ Spring Security + JWT (JSON Web Token)

**선택 이유:**
- **Spring Security**:
  - Spring 생태계와 완벽한 통합
  - 필터 체인 기반으로 인증/인가 로직을 분리하여 유지보수성 향상
  - CSRF, XSS 등 보안 취약점 방어 기능 내장
  
- **JWT**:
  - **Stateless**: 서버에 세션을 저장하지 않아 수평 확장(Scale-out)에 유리
  - **모바일 친화적**: iOS 클라이언트가 간편하게 토큰을 헤더에 포함하여 API 호출 가능
  - **Refresh Token**: Access Token 만료 시 재로그인 없이 갱신 가능

#### ✅ OAuth 2.0 (카카오, 구글, 네이버)

**선택 이유:**
- **사용자 편의성**: 별도 회원가입 없이 소셜 계정으로 간편 로그인
- **보안**: 비밀번호를 직접 저장하지 않아 유출 위험 감소
- **신뢰성**: 대형 플랫폼의 인증 시스템을 활용하여 안정성 확보

---

### 1.4 AI & External API

#### ✅ OpenAI API (GPT-4o) + Spring AI

**선택 이유:**
- **OpenAI API**:
  - 가장 강력한 자연어 처리 성능을 가진 GPT-4o 모델 사용
  - 사용자의 감정과 고민을 깊이 있게 이해하고 공감적인 답변 생성
  - 심리 상담 특화 프롬프트 엔지니어링으로 맞춤형 상담 제공
  
- **Spring AI**:
  - Spring Boot 환경에서 OpenAI API를 쉽게 통합
  - 프롬프트 템플릿 관리, 대화 히스토리 유지 등 편의 기능 제공
  - 향후 Claude, Gemini 등 다른 LLM으로 전환 시 코드 변경 최소화

#### ✅ WebFlux (비동기 HTTP 클라이언트)

**선택 이유:**
- **비동기 처리**: OpenAI API 호출 시 블로킹 없이 다른 요청 처리 가능
- **성능**: 동시 다발적인 AI 상담 요청에도 빠른 응답 속도 유지
- **효율성**: 스레드 사용량 최소화로 서버 리소스 절약

---

### 1.5 Storage & File Management

#### ✅ AWS S3

**선택 이유:**
- **확장성**: 용량 제한 없이 이미지 파일 저장 가능 (미션 사진, 프로필 사진 등)
- **안정성**: 99.999999999%의 내구성으로 데이터 손실 위험 최소화
- **CDN 연동**: CloudFront와 결합하여 전 세계 어디서나 빠른 이미지 로딩
- **비용 효율**: 사용한 만큼만 과금되는 종량제 요금제

---

### 1.6 Monitoring & Observability

#### ✅ Spring Boot Actuator + Prometheus

**선택 이유:**
- **Actuator**:
  - 애플리케이션 상태 모니터링(health check, metrics)
  - `/actuator/health`로 서버 헬스 체크 자동화
  
- **Prometheus**:
  - 시계열 데이터베이스로 메트릭 수집 및 저장
  - Grafana와 연동하여 실시간 대시보드 구축 가능
  - API 응답 시간, 에러율, DB 커넥션 풀 등 성능 지표 시각화

---

### 1.7 Documentation

#### ✅ Swagger (SpringDoc OpenAPI 3.0)

**선택 이유:**
- **자동 문서화**: Controller 어노테이션만으로 API 명세 자동 생성
- **팀 협업**: iOS 팀이 실시간으로 API 스펙 확인하고 테스트 가능
- **API 테스트**: Swagger UI에서 직접 API 호출 테스트 가능하여 Postman 대체
- **버전 관리**: API 변경 시 자동으로 문서도 업데이트되어 문서 불일치 방지

---

### 1.8 Validation

#### ✅ Spring Validation (Bean Validation 2.0)

**선택 이유:**
- **선언적 검증**: `@NotBlank`, `@Valid` 등 어노테이션으로 입력값 검증
- **일관성**: Controller, Service, Entity 모든 계층에서 동일한 검증 로직 적용
- **보안**: SQL Injection, XSS 등 악의적인 입력값 사전 차단

---

## 2. API 설계 목적 및 구조

### 2.1 설계 목적

#### 🎯 핵심 목표
1. **사용자 중심 설계**: 직관적인 엔드포인트 네이밍으로 iOS 팀이 쉽게 이해하고 사용
2. **RESTful 원칙 준수**: HTTP 메서드(GET, POST, PATCH, DELETE)를 명확히 구분
3. **확장 가능한 구조**: 새로운 기능 추가 시 기존 API에 영향 없이 확장 가능
4. **일관된 응답 포맷**: 모든 API가 동일한 구조의 응답 반환 (`ApiResponse<T>`)
5. **보안**: JWT 기반 인증으로 사용자별 데이터 격리

#### 📦 응답 포맷 통일

**성공 응답 (ApiResponse<T>)**
```json
{
  "isSuccess": true,
  "code": "SUCCESS_200",
  "message": "요청 성공",
  "data": { ... }
}
```

**실패 응답 (ErrorResponse)**
```json
{
  "isSuccess": false,
  "code": "USER4041",
  "message": "사용자를 찾을 수 없습니다.",
  "httpStatus": 404
}
```

---

### 2.2 아키텍처 구조

#### 🏛️ 3-Layer Architecture (MVC + Service)

```
┌─────────────┐
│ Controller  │ ← iOS 클라이언트 요청 수신 및 응답 반환
└──────┬──────┘
       │
┌──────▼──────┐
│  Service    │ ← 비즈니스 로직 처리 (AI 상담, 미션 할당 등)
└──────┬──────┘
       │
┌──────▼──────┐
│ Repository  │ ← JPA를 통한 DB 접근 (CRUD)
└──────┬──────┘
       │
┌──────▼──────┐
│   MySQL     │ ← 데이터 영속화
└─────────────┘
```

#### 📁 도메인 주도 설계 (DDD)

```
src/main/java/com/symteo/
├── domain/
│   ├── user/           # 사용자 관리 (프로필, 설정)
│   ├── counsel/        # AI 상담
│   ├── diagnose/       # 심리 진단 테스트
│   ├── todayMission/   # 오늘의 미션
│   ├── home/           # 홈 화면 대시보드
│   └── report/         # 진단 리포트
└── global/
    ├── auth/           # 인증/인가 (OAuth, JWT)
    ├── config/         # 전역 설정 (Security, Swagger)
    └── ApiPayload/     # 공통 응답 포맷
```

**도메인별 패키지 구조 (예: user/)**
```
domain/user/
├── controller/     # API 엔드포인트 정의
├── service/        # 비즈니스 로직
├── repository/     # DB 접근 인터페이스
├── entity/         # JPA Entity (User, UserSettings 등)
├── dto/            # 요청/응답 DTO
├── enums/          # Enum 타입 (권한, 상태 등)
└── exception/      # 도메인별 예외 처리
```

---

## 3. 도메인별 API 상세 설명

### 3.1 인증/인가 (Authentication & Authorization)

**도메인**: `global.auth`  
**담당**: 젬마 (황예원)

#### 📌 설계 목적
- 소셜 로그인(카카오, 구글, 네이버)을 통한 간편 회원가입/로그인
- JWT 기반 Stateless 인증으로 서버 부하 최소화
- Refresh Token으로 사용자 경험 개선 (재로그인 최소화)

#### 🔧 주요 기술 스택
- **Spring Security**: 필터 체인 기반 인증/인가
- **JWT (jjwt 0.9.1)**: Access Token(30분) + Refresh Token(7일)
- **OAuth 2.0**: 카카오/구글/네이버 OAuth 클라이언트
- **Redis**: Refresh Token 저장 및 로그아웃 처리

#### 🔑 핵심 API

| HTTP Method | Endpoint | 설명 | 인증 필요 |
|-------------|----------|------|-----------|
| `POST` | `/api/v1/auth/login/{provider}` | 소셜 로그인 (가입/로그인 통합) | ❌ |
| `POST` | `/api/v1/auth/refresh` | Access Token 재발급 | ❌ |
| `POST` | `/api/v1/auth/logout` | 로그아웃 (Refresh Token 무효화) | ✅ |
| `DELETE` | `/api/v1/auth/withdraw` | 회원 탈퇴 | ✅ |

#### 📝 설계 이유
- **통합 로그인 엔드포인트**: 회원가입/로그인을 하나의 API로 처리하여 iOS 팀의 복잡도 감소
- **Refresh Token 전략**: 보안(짧은 Access Token 유효기간)과 편의성(재로그인 최소화)의 균형
- **Redis 활용**: 로그아웃 시 Refresh Token을 블랙리스트에 등록하여 토큰 재사용 방지

---

### 3.2 사용자 관리 (User)

**도메인**: `domain.user`  
**담당**: 니카 (이나경)

#### 📌 설계 목적
- 사용자 프로필 및 환경설정 관리
- 완료한 미션 히스토리 조회 및 수정
- AI 상담사 성향 커스터마이징

#### 🔧 주요 기술 스택
- **Spring Data JPA**: User, UserSettings, UserMissions 엔티티 관리
- **Validation**: `@Pattern`으로 닉네임 정규식 검증 (한글/영문/숫자 3~10자)
- **AWS S3**: 미션 이미지 업로드/수정/삭제
- **트랜잭션 관리**: `@Transactional`로 데이터 일관성 보장

#### 🔑 핵심 API

| HTTP Method | Endpoint | 설명 | 비고 |
|-------------|----------|------|------|
| `GET` | `/api/v1/users/check-nickname` | 닉네임 중복 확인 | 정규식 검증 포함 |
| `POST` | `/api/v1/users/signup` | 회원가입 완료 (닉네임 설정) | GUEST → USER 권한 승격 |
| `GET` | `/api/v1/users/profile` | 프로필 조회 | 닉네임, 프로필 사진 |
| `PATCH` | `/api/v1/users/nickname` | 닉네임 수정 | 중복 체크 재검증 |
| `GET` | `/api/v1/users/settings` | 환경설정 조회 | 알림, 앱 잠금 등 |
| `PATCH` | `/api/v1/users/settings` | 환경설정 수정 | Boolean 토글 값 |
| `GET` | `/api/v1/users/counselor-settings` | AI 상담사 설정 조회 | 분위기, 지원 방식 등 |
| `PATCH` | `/api/v1/users/counselor-settings` | AI 상담사 설정 수정 | Enum 타입 검증 |
| `GET` | `/api/v1/users/missions/history` | 완료한 미션 리스트 | 완료일 기준 내림차순 |
| `GET` | `/api/v1/users/missions/history/{id}` | 특정 미션 상세 조회 | 내용 + 이미지 |
| `PATCH` | `/api/v1/users/missions/history/{id}` | 미션 수정 | 내용/이미지 선택적 수정 |

#### 📝 설계 이유

**1. 닉네임 중복 확인 + 정규식 검증**
- **문제**: 부적절한 닉네임(특수문자, 긴 문자열) 저장 방지
- **해결**: Service 계층에서 정규식 검증 후 DB 중복 체크
- **코드**:
  ```java
  private static final Pattern NICKNAME_PATTERN = 
      Pattern.compile("^[가-힣a-zA-Z0-9]{3,10}$");
  
  if (!NICKNAME_PATTERN.matcher(nickname).matches()) {
      throw new UserException(UserErrorCode._NICKNAME_INVALID);
  }
  ```

**2. 미션 수정 API - 선택적 파라미터 처리**
- **문제**: 사용자가 내용만 수정할 수도, 이미지만 수정할 수도, 둘 다 수정할 수도 있음
- **해결**: 
  - `@RequestPart(required = false)`로 선택적 파라미터 설정
  - Service 계층에서 null 체크 후 선택적 업데이트
- **로직**:
  ```
  - request = null, images = null → 아무것도 수정 안 함
  - request ≠ null, images = null → 내용만 수정
  - request = null, images = [] → 기존 이미지 전부 삭제
  - request ≠ null, images = [파일들] → 내용 + 이미지 모두 수정
  ```

**3. 도메인별 예외 처리 (UserException)**
- **문제**: 모든 예외를 `GeneralException`으로 처리하면 에러 추적 어려움
- **해결**: 
  - `UserErrorCode` Enum으로 도메인별 에러 코드 정의
  - `UserException` 클래스로 User 도메인 전용 예외 처리
- **장점**:
  - 에러 코드 중복 방지 (USER4041, MISSION4042 등)
  - iOS 팀이 에러 코드만 보고 어떤 도메인에서 발생한 에러인지 즉시 파악

---

### 3.3 AI 상담 (Counsel)

**도메인**: `domain.counsel`  
**담당**: 김찌 (전형진)

#### 📌 설계 목적
- 사용자의 고민을 듣고 공감하는 AI 상담사 제공
- 사용자가 설정한 상담사 성향(분위기, 지원 방식 등)에 맞춰 답변 생성
- 대화 히스토리 저장 및 요약 제공

#### 🔧 주요 기술 스택
- **OpenAI API (GPT-4o)**: 자연어 처리 및 감정 분석
- **Spring AI**: 프롬프트 템플릿 관리 및 OpenAI API 통합
- **JPA Entity**: ChatRoom, ChatMessage, CounselorSettings
- **프롬프트 엔지니어링**: 심리 상담 특화 시스템 프롬프트 설계

#### 🔑 핵심 API

| HTTP Method | Endpoint | 설명 | 비고 |
|-------------|----------|------|------|
| `POST` | `/api/v1/counsels` | AI 상담 메시지 전송 | 대화 히스토리 유지 |
| `POST` | `/api/v1/counsels/report` | 리포트 기반 상담 요청 | 진단 결과 + 미션 기록 분석 |
| `PATCH` | `/api/v1/counsels/{counselId}/summary` | 상담 종료 및 요약 생성 | GPT가 대화 내용 요약 |
| `GET` | `/api/v1/counsels` | 전체 상담 목록 조회 | 날짜 순 정렬 |
| `GET` | `/api/v1/counsels/{counselId}` | 특정 상담 상세 조회 | 전체 대화 내용 |
| `DELETE` | `/api/v1/counsels/{counselId}` | 상담 삭제 | Soft Delete |
| `PUT` | `/api/v1/counsels/setting` | 상담사 초기 설정 저장 | 첫 상담 전 필수 |

#### 📝 설계 이유

**1. 상담사 성향 커스터마이징**
- **문제**: 모든 사용자에게 동일한 톤앤매너로 상담하면 공감도 낮음
- **해결**: 
  - `CounselorSettings` 엔티티로 사용자별 상담사 성향 저장
  - 4가지 Enum 타입 설정 (분위기, 지원 방식, 역할, 답변 형식)
- **예시**:
  ```
  분위기(Atmosphere): WARM(따뜻한) / CALM(차분한) / BRIGHT(밝은)
  지원 방식(Support_Style): EMPATHIC(공감형) / SOLUTION(해결형) / LISTENING(경청형)
  역할(Counselor_Role): FRIEND(친구) / PROFESSIONAL(전문가) / MENTOR(멘토)
  답변 형식(Answer_Format): SHORT(짧고 간결) / DETAILED(상세) / STEP_BY_STEP(단계별)
  ```

**2. 대화 히스토리 유지 (Context Window)**
- **문제**: 이전 대화를 기억하지 못하면 맥락 없는 답변 생성
- **해결**: 
  - `ChatMessage` 엔티티로 모든 대화 저장 (role: USER/ASSISTANT)
  - OpenAI API 호출 시 최근 N개 메시지를 함께 전송
- **성능 최적화**:
  ```
  - 최근 20개 메시지만 전송 (토큰 제한 고려)
  - 요약 기능으로 긴 대화는 압축하여 저장
  ```

**3. 리포트 기반 상담**
- **문제**: 사용자가 "내 리포트 보고 조언해줘" 요청 시 어떻게 처리?
- **해결**: 
  - 진단 리포트 데이터를 프롬프트에 포함하여 OpenAI에 전송
  - 예시: "사용자의 우울 점수는 15점이고, 최근 3일간 '슬픔' 감정이 많았습니다. 이에 맞춰 조언해주세요."
- **장점**: 단순 대화가 아닌 데이터 기반 맞춤형 상담 제공

**4. 상담 종료 및 요약**
- **문제**: 긴 상담 내용을 나중에 다시 보기 어려움
- **해결**: 
  - 상담 종료 시 GPT에게 "이 대화를 3줄로 요약해줘" 요청
  - 요약본을 `ChatRoom.summary`에 저장
- **UI 활용**: 상담 목록에서 요약본만 보여줘 사용자가 빠르게 내용 파악

---

### 3.4 심리 진단 테스트 (Diagnose)

**도메인**: `domain.diagnose`  
**담당**: 김찌 (전형진)

#### 📌 설계 목적
- PHQ-9(우울), GAD-7(불안) 등 표준 척도 기반 진단
- 진단 결과를 AI 상담 및 미션 추천에 활용
- 진단 히스토리 저장으로 심리 상태 변화 추적

#### 🔧 주요 기술 스택
- **JPA Entity**: Diagnose, Tests (진단-문항 다대다 관계)
- **Enum**: 진단 유형 (DEPRESSION, ANXIETY, STRESS, ATTACHMENT)
- **계산 로직**: 척도별 점수 합산 및 등급 판정

#### 🔑 핵심 API

| HTTP Method | Endpoint | 설명 | 비고 |
|-------------|----------|------|------|
| `POST` | `/api/v1/diagnoses` | 진단 생성 (문항 제출) | 점수 자동 계산 |
| `GET` | `/api/v1/diagnoses` | 전체 진단 목록 조회 | 진단 유형별 필터링 |
| `GET` | `/api/v1/diagnoses/{diagnoseId}` | 특정 진단 상세 조회 | 문항별 답변 포함 |
| `DELETE` | `/api/v1/diagnoses/{diagnoseId}` | 진단 삭제 | Soft Delete |

#### 📝 설계 이유

**1. 표준 척도 기반 설계**
- **이유**: 의학적으로 검증된 척도 사용으로 신뢰성 확보
- **구현**:
  ```
  PHQ-9 (우울증): 9개 문항, 각 0~3점 → 총점 0~27점
  - 0~4점: 정상
  - 5~9점: 가벼운 우울
  - 10~14점: 중간 정도 우울
  - 15~19점: 중증 우울
  - 20~27점: 심각한 우울
  ```

**2. 진단 결과 활용**
- **미션 추천**: 우울 점수 높으면 → "산책하기", "햇빛 쬐기" 미션 우선 추천
- **AI 상담**: 진단 결과를 프롬프트에 포함하여 맞춤형 상담 제공
- **리포트 생성**: 진단 데이터를 시각화하여 월간 리포트 작성

**3. 진단 히스토리 저장**
- **문제**: 사용자가 정기적으로 진단을 받을 때 이전 결과와 비교 필요
- **해결**: 
  - `Diagnose` 엔티티에 `createdAt` 필드로 진단 시점 저장
  - 월별 진단 결과를 차트로 시각화 (예: 1월 15점 → 2월 10점 → 개선 추세)

---

### 3.5 오늘의 미션 (Today Mission)

**도메인**: `domain.todayMission`  
**담당**: 에코 (김민지)

#### 📌 설계 목적
- 사용자의 심리 상태에 맞춘 일일 미션 제공
- 미션 수행 기록을 저장하여 리포트 생성에 활용
- 임시저장 기능으로 사용자 편의성 향상

#### 🔧 주요 기술 스택
- **JPA Entity**: Mission, UserMissions, Drafts, MissionImages
- **AWS S3**: 미션 인증 사진 업로드
- **Multipart File**: `@RequestPart`로 이미지 + JSON 동시 전송
- **스케줄링**: Spring Scheduler로 자정마다 미션 자동 할당 (추후 구현 예정)

#### 🔑 핵심 API

| HTTP Method | Endpoint | 설명 | 비고 |
|-------------|----------|------|------|
| `GET` | `/api/v1/missions/today` | 오늘의 미션 조회 | 진단 결과 기반 추천 |
| `POST` | `/api/v1/missions/{missionId}/submissions` | 미션 시작 (이미지 제출) | Multipart 요청 |
| `POST` | `/api/v1/missions/{userMissionId}/drafts` | 임시저장 | 내용만 저장 |
| `PATCH` | `/api/v1/missions/{userMissionId}/status` | 미션 완료 처리 | isCompleted = true |
| `PATCH` | `/api/v1/missions/today-mission/refresh` | 오늘 미션 새로고침 | 하루 1회 제한 |

#### 📝 설계 이유

**1. 진단 기반 미션 추천 알고리즘**
- **문제**: 모든 사용자에게 동일한 미션 제공하면 효과 낮음
- **해결**: 
  - 최근 진단 결과를 분석하여 미션 우선순위 결정
  - 예시:
    ```
    우울 점수 15점 이상 → "산책하기", "햇빛 쬐기" 카테고리 우선
    불안 점수 10점 이상 → "호흡 명상", "요가" 카테고리 우선
    스트레스 점수 높음 → "취미 활동", "음악 듣기" 카테고리 우선
    ```

**2. Multipart 요청 처리 (이미지 + JSON)**
- **문제**: 미션 제출 시 텍스트(일기)와 이미지를 함께 전송해야 함
- **해결**:
  ```java
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ApiResponse<UserMissionStartResponse> startMission(
      @RequestPart("request") @Valid UserMissionStartRequest request,
      @RequestPart(value = "image", required = false) MultipartFile image
  ) { ... }
  ```
- **장점**: 
  - iOS에서 FormData로 한 번에 전송 가능
  - S3 업로드와 DB 저장을 하나의 트랜잭션으로 처리

**3. 임시저장 기능**
- **문제**: 사용자가 미션 작성 중 앱을 종료하면 내용 손실
- **해결**: 
  - `Drafts` 엔티티로 임시저장 내용 관리
  - 사용자가 다시 앱을 열면 임시저장 내용 불러오기
- **DB 설계**:
  ```sql
  Drafts (
    draft_id, 
    user_mission_id, 
    contents, 
    created_at, 
    updated_at
  )
  ```

**4. 미션 새로고침 제한**
- **문제**: 사용자가 원하는 미션이 나올 때까지 무한 새로고침 가능
- **해결**: 
  - `UserMissions.lastRefreshDate` 필드로 마지막 새로고침 시간 저장
  - 하루 1회만 새로고침 가능하도록 검증
- **코드**:
  ```java
  if (userMission.getLastRefreshDate().isEqual(LocalDate.now())) {
      throw new TodayMissionException(TodayMissionErrorCode._MISSION_REFRESH_EXCEEDED);
  }
  ```

---

### 3.6 홈 화면 대시보드 (Home)

**도메인**: `domain.home`  
**담당**: 에코 (김민지)

#### 📌 설계 목적
- 사용자의 오늘 상태를 한눈에 파악할 수 있는 대시보드 제공
- 오늘의 감정 날씨, 미션 진행 상황, 최근 진단 결과 통합 조회

#### 🔧 주요 기술 스택
- **JPA Entity**: TodayEmotion, UserMissions, Diagnose
- **DTO 통합**: 여러 도메인 데이터를 하나의 Response로 조합
- **캐싱**: Redis로 자주 조회되는 홈 데이터 캐싱 (추후 구현 예정)

#### 🔑 핵심 API

| HTTP Method | Endpoint | 설명 | 비고 |
|-------------|----------|------|------|
| `GET` | `/api/v1/home` | 홈 화면 전체 데이터 조회 | 감정, 미션, 진단 통합 |
| `PUT` | `/api/v1/home/today-emotions` | 오늘의 감정 날씨 수정/생성 | Upsert 방식 |

#### 📝 설계 이유

**1. 통합 조회 API (N+1 문제 해결)**
- **문제**: 
  - 감정, 미션, 진단을 각각 조회하면 3번의 API 호출 필요
  - iOS 앱 로딩 시간 증가
- **해결**: 
  - `/api/v1/home` 하나로 모든 데이터 조회
  - JPA Fetch Join으로 N+1 쿼리 문제 해결
- **응답 구조**:
  ```json
  {
    "todayEmotion": { "weather": "SUNNY", "date": "2026-02-12" },
    "todayMission": { "title": "산책하기", "isCompleted": false },
    "recentDiagnose": { "type": "DEPRESSION", "score": 15, "level": "MODERATE" },
    "weeklyProgress": { "completed": 5, "total": 7 }
  }
  ```

**2. 감정 날씨 Upsert**
- **문제**: 사용자가 오늘 감정을 여러 번 수정할 수 있음
- **해결**: 
  - `PUT` 메서드로 있으면 업데이트, 없으면 생성
  - 하루 1개의 감정 기록만 유지 (날짜를 Unique Key로 설정)
- **코드**:
  ```java
  TodayEmotion emotion = todayEmotionRepository
      .findByUserIdAndDate(userId, LocalDate.now())
      .orElse(TodayEmotion.builder()
          .user(user)
          .date(LocalDate.now())
          .build());
  emotion.updateWeather(weather);
  ```

---

### 3.7 진단 리포트 (Report)

**도메인**: `domain.report`  
**담당**: 에코 (김민지)

#### 📌 설계 목적
- 진단 결과를 시각화하여 사용자가 자신의 심리 상태를 이해하도록 돕기
- 월간/주간 리포트로 변화 추이 분석
- AI 분석을 통한 맞춤형 피드백 제공

#### 🔧 주요 기술 스택
- **OpenAI API**: 진단 결과 분석 및 피드백 생성
- **JPA Entity**: DepressionAnxietyReports, StressReports, AttachmentReports
- **Chart 데이터**: 시계열 데이터를 JSON 배열로 반환

#### 🔑 핵심 API

| HTTP Method | Endpoint | 설명 | 비고 |
|-------------|----------|------|------|
| `POST` | `/api/v1/reports/diagnose/{diagnoseId}/depression-anxiety` | 우울/불안 리포트 생성 | GPT 분석 포함 |
| `GET` | `/api/v1/reports/depression-anxiety/{reportId}` | 우울/불안 리포트 조회 | 차트 데이터 포함 |
| `POST` | `/api/v1/reports/diagnose/{diagnoseId}/stress-burnout` | 스트레스/번아웃 리포트 생성 | GPT 분석 포함 |
| `GET` | `/api/v1/reports/stress-burnout/{reportId}` | 스트레스/번아웃 리포트 조회 | 차트 데이터 포함 |
| `POST` | `/api/v1/reports/diagnose/{diagnoseId}/attachment` | 성향 리포트 생성 | 애착 유형 분석 |
| `GET` | `/api/v1/reports/attachment/{reportId}` | 성향 리포트 조회 | 애착 유형별 설명 |

#### 📝 설계 이유

**1. GPT 기반 리포트 생성**
- **문제**: 단순 점수만 보여주면 사용자가 이해하기 어려움
- **해결**: 
  - 진단 결과를 OpenAI API에 전송하여 해석 요청
  - 예시 프롬프트:
    ```
    사용자의 PHQ-9 점수는 15점입니다. 이는 중간 정도의 우울 상태입니다.
    다음을 포함하여 200자 이내로 분석해주세요:
    1. 현재 상태 설명
    2. 주의해야 할 점
    3. 추천 활동
    ```

**2. 시계열 차트 데이터**
- **문제**: 진단 결과가 개선되고 있는지 한눈에 파악하기 어려움
- **해결**: 
  - 최근 30일간의 진단 결과를 배열로 반환
  - iOS에서 Chart 라이브러리로 시각화
- **응답 예시**:
  ```json
  {
    "chartData": [
      { "date": "2026-01-15", "score": 15 },
      { "date": "2026-02-01", "score": 12 },
      { "date": "2026-02-12", "score": 10 }
    ]
  }
  ```

**3. 리포트 유형 분리**
- **이유**: 
  - 우울/불안, 스트레스, 성향은 각각 다른 척도와 해석 방법 사용
  - 도메인별로 Entity와 Service를 분리하여 유지보수성 향상
- **설계**:
  ```
  DepressionAnxietyReportsService → PHQ-9, GAD-7 분석
  StressReportsService → PSS, Burnout 척도 분석
  AttachmentReportsService → 애착 유형 분석
  ```

---

## 4. 아키텍처 설계 원칙

### 4.1 단일 책임 원칙 (Single Responsibility Principle)

- **Controller**: 요청/응답 변환만 담당 (비즈니스 로직 없음)
- **Service**: 비즈니스 로직 처리 (트랜잭션 관리)
- **Repository**: DB 접근만 담당

### 4.2 의존성 역전 원칙 (Dependency Inversion Principle)

- **인터페이스 활용**: `Repository`, `Service`를 인터페이스로 정의하여 구현체 교체 용이
- **테스트 가능성**: Mock 객체로 단위 테스트 작성 가능

### 4.3 DRY 원칙 (Don't Repeat Yourself)

- **공통 응답 포맷**: `ApiResponse<T>` 클래스로 모든 API 응답 통일
- **예외 처리**: `GlobalExceptionHandler`로 중복 예외 처리 코드 제거
- **Validation**: `@Valid` 어노테이션으로 반복적인 검증 코드 제거

### 4.4 확장 가능한 설계

- **도메인별 패키지 분리**: 새로운 기능 추가 시 독립적으로 개발 가능
- **Enum 활용**: 상태, 타입 등을 Enum으로 관리하여 추가/수정 용이
- **JPA Auditing**: `BaseEntity`로 createdAt, updatedAt 자동 관리

---

## 📚 참고 자료

- [Spring Boot 공식 문서](https://spring.io/projects/spring-boot)
- [Spring Security 레퍼런스](https://docs.spring.io/spring-security/reference/index.html)
- [OpenAI API 문서](https://platform.openai.com/docs/api-reference)
- [AWS S3 SDK for Java](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/home.html)
- [JWT 표준 (RFC 7519)](https://datatracker.ietf.org/doc/html/rfc7519)
- [PHQ-9 척도](https://www.apa.org/depression-guideline/patient-health-questionnaire.pdf)

---

## 📝 변경 이력

| 날짜 | 작성자 | 내용 |
|------|--------|------|
| 2026-02-12 | 니카 (이나경) | 최초 작성 - 기술 스택 및 API 설계 문서화 |

