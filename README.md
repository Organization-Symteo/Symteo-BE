# 🌿 Symteo (심터) - Back-End

> **답답한 마음, 심터에서 얘기해봐요**
> **심터는 사용자의 심리 진단 데이터를 기반으로 맞춤형 AI 상담과 일일 미션을 제공하는 멘탈 케어 플랫폼입니다.**

---

## 🫂 1. 팀원 정보 및 역할

| 이름 | 역할 | 담당 기능 (RQ-ID) |
| --- | --- | --- |
| **니카 (이나경)** | **BE Lead** | My심터, 알림 설정, 앱 잠금, 회원 탈퇴 |
| **젬마 (황예원)** | **Developer** | 로그인/로그아웃, 회원가입, 온보딩 가이드 |
| **김찌 (전형진)** | **Developer** | AI 맞춤 상담, 심리 진단 테스트 |
| **에코 (김민지)** | **Developer** | 오늘의 미션, 홈 화면 인터랙션, 진단 리포트 |

---

## 💡 2. 서비스 개요 (Service Overview)

현대인의 마음 짐을 덜어주기 위해 **데이터 기반 진단**과 **감성적인 AI 상담**을 결합했습니다.

* **To. 사용자**: PHQ-9 등 표준 척도를 통한 정밀 진단과 나만의 성향에 맞는 AI 상담사와의 대화
* **To. 지속성**: 매일 제공되는 맞춤 미션과 시각화 리포트를 통한 심리 상태 관리

---

## 📌 3. 기술 스택 (Tech Stack)

* **Framework/Language**: Spring Boot 3.x, Java 17
* **Build/Database**: Gradle, MySQL, Spring Data JPA
* **AI & Security**: OpenAI API (GPT-4o), JWT 기반 소셜 로그인
* **Docs**: Swagger (SpringDoc)

---

## 📂 4. 프로젝트 구조 및 컨벤션 (Project Structure)

본 프로젝트는 **Service-Oriented Architecture (SOA)**와 **MVC 패턴**을 따릅니다.

### 패키지 구조

```text
Symteo-BE/
├── 📁 .github/             # Issue/PR 템플릿 및 CI 설정
├── 📁 src/main/java/com/symteo/
│   ├── 📁 config/      # 전역 설정 (Security, Swagger, AI API)
│   ├── 📁 controller/  # iOS 요청 수신 및 응답 반환
│   ├── 📁 service/     # 비즈니스 로직 (AI 상담 엔진, 척도 계산)
│   ├── 📁 domain/      # JPA Entity (데이터 모델)
│   ├── 📁 repository/  # DB 접근 계층
│   ├── 📁 dto/         # Request/Response 객체 및 Validation
│   └── 📁 global/      # ExceptionHandler, 공통 Util

```

### 코드 스타일 및 규칙

* **기본 스타일**: 들여쓰기 Space 2칸, 문자열 Double Quote(`" "`) 사용
* **커밋 메시지**: `타입: 내용 (#이슈번호)` (예: `feat: add AI chat logic (#12)`)
* **브랜치 전략**: `이름/이슈번호/기능명` (예: `niicka/12/lock-settings`)

---

## 🤝 5. PR 및 머지 규칙

* **승인 조건**: 리뷰어 **1명 이상의 승인(Approve)** 필수
* **머지 방식**: **Squash & Merge** (커밋 히스토리 관리)
* **임의 머지**: 오타, 단순 주석, README 수정 시에만 가능 (사후 보고 필수)
