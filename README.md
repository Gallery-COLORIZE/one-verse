<div align="center">

# OneVerse

AI로 말씀을 더 가까이, 일상 속 묵상을 더 자연스럽게.

![Kotlin](https://img.shields.io/badge/Kotlin-2.3.21-7F52FF?logo=kotlin&logoColor=white)
![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-6DB33F?logo=springboot&logoColor=white)
![Spring AI](https://img.shields.io/badge/Spring%20AI-2.0.0-6DB33F)
![Version](https://img.shields.io/badge/version-1.0.0-111827)
![Docker](https://img.shields.io/badge/Docker-ready-2496ED?logo=docker&logoColor=white)

</div>

---

## Overview

OneVerse는 AI가 신앙생활에 긍정적인 영향을 줄 수 있다는 가정에서 출발한 말씀 추천 서비스입니다. 사용자의 하루나 현재 상황을 바탕으로 묵상할 만한 성경 구절과 짧은 메시지를 제공해, 말씀을 더 자주 떠올리고 삶의 자리에서 적용하도록 돕는 것을 목표로 합니다.

> OneVerse는 AI가 신앙을 대신하게 하는 서비스가 아닙니다.  
> AI를 통해 사용자가 다시 말씀, 기도, 공동체로 연결되도록 돕는 보조 도구입니다.

## Contents

- [기획 의도](#기획-의도)
- [핵심 가치](#핵심-가치)
- [현재 기능](#현재-기능)
- [사용자 흐름](#사용자-흐름)
- [기술 스택](#기술-스택)
- [프로젝트 구조](#프로젝트-구조)
- [실행 방법](#실행-방법)
- [CI/CD](#cicd)
- [AI 응답 원칙](#ai-응답-원칙)
- [주의 사항](#주의-사항)
- [로드맵](#로드맵)

## 기획 의도

현대인의 신앙생활은 바쁜 일상, 감정적 고립, 말씀 묵상의 지속성 부족 같은 문제를 자주 마주합니다. OneVerse는 AI를 목회자나 공동체의 대체재로 사용하지 않고, 말씀과 기도로 다시 연결되도록 돕는 보조 도구로 설계합니다.

| 핵심 가정 | 설명 |
| --- | --- |
| 상황 이해 | AI는 사용자의 상황을 빠르게 정리하고 적절한 묵상 주제를 제안할 수 있습니다. |
| 말씀 접근성 | 개인화된 말씀 안내는 성경을 더 가까이 접하는 계기가 될 수 있습니다. |
| 신앙적 검증 | 신앙적 판단, 상담, 권면은 성경과 교회 공동체 안에서 검증되어야 합니다. |

## 핵심 가치

| 가치 | 방향 |
| --- | --- |
| 말씀 중심 | 사용자의 감정이나 상황보다 성경 본문과 복음적 메시지를 우선합니다. |
| 보조적 역할 | AI 응답은 신앙 결정을 대신하지 않고 묵상을 돕는 참고 자료로 사용합니다. |
| 따뜻한 전달 | 정죄하거나 단정하기보다 위로, 회개, 소망, 순종으로 이어지는 언어를 지향합니다. |
| 검증 가능성 | 추천 구절의 출처를 함께 제공해 사용자가 직접 성경에서 확인할 수 있게 합니다. |

## 현재 기능

- 오늘의 말씀 추천
- 사용자가 입력한 상황 기반 말씀 추천
- AI가 생성한 짧은 묵상 메시지 제공
- Thymeleaf 기반 단일 웹 화면
- Google GenAI 모델 연동

## 사용자 흐름

```text
사용자 선택
   |
   |-- 오늘의 말씀
   |-- 상황 입력
          |
          v
Spring MVC Controller
          |
          v
BibleVerseService
          |
          v
Spring AI + Google GenAI
          |
          v
묵상 메시지 + 성경 구절 출처
```

1. 사용자는 `오늘의 말씀` 또는 `상황 입력`을 선택합니다.
2. 상황 입력을 선택한 경우 현재 고민, 감정, 기도 제목 등을 작성합니다.
3. 서버는 Spring AI를 통해 AI 모델에 요청을 보냅니다.
4. AI는 한 문장의 묵상 메시지와 성경 구절 출처를 반환합니다.
5. 사용자는 응답을 바탕으로 성경 본문을 확인하고 묵상합니다.

## 기술 스택

| 영역 | 기술 |
| --- | --- |
| Language | Kotlin, Java 21 |
| Framework | Spring Boot, Spring MVC |
| View | Thymeleaf |
| AI | Spring AI, Google GenAI Gemini |
| Build | Gradle Kotlin DSL |
| Infra | Docker |

## 프로젝트 구조

```text
.
├── build.gradle.kts
├── Dockerfile
├── settings.gradle.kts
└── src
    ├── main
    │   ├── kotlin/com/neobible/oneverse
    │   │   ├── OneVerseApplication.kt
    │   │   ├── controller/BibleVerseController.kt
    │   │   └── service/BibleVerseService.kt
    │   └── resources
    │       ├── application.yml
    │       └── templates/index.html
    └── test/kotlin/com/neobible/oneverse
        ├── controller/BibleVerseControllerTest.kt
        └── service/BibleVerseServiceTest.kt
```

## 실행 방법

### 1. 환경 변수 설정

Google GenAI API 키가 필요합니다.

PowerShell:

```powershell
$env:GOOGLE_API_KEY="your-google-genai-api-key"
```

macOS/Linux:

```bash
export GOOGLE_API_KEY="your-google-genai-api-key"
```

### 2. 로컬 실행

```bash
./gradlew bootRun
```

Windows PowerShell:

```powershell
.\gradlew.bat bootRun
```

브라우저에서 접속합니다.

```text
http://localhost:8080
```

### 3. 테스트

```bash
./gradlew test
```

Windows PowerShell:

```powershell
.\gradlew.bat test
```

### 4. Docker 실행

```bash
docker build -t isaiahim0214/colorize-neo-bible-oneverse:1.0.0 .
docker run --rm -p 8080:8080 -e GOOGLE_API_KEY="your-google-genai-api-key" isaiahim0214/colorize-neo-bible-oneverse:1.0.0
```

## CI/CD

GitHub Actions 워크플로는 `.github/workflows/ci-cd.yml`에서 관리합니다. `main` 브랜치에 커밋이 push되면 전체 테스트, Docker 이미지 빌드 및 push, OpenVPN 내부망 접속, SSH 서버 배포, 배포 완료 확인 순서로 실행됩니다.

> Docker 이미지 이름은 Docker registry 규칙에 맞춰 소문자인 `isaiahim0214/colorize-neo-bible-oneverse`를 사용합니다.

### Pipeline

| 단계 | 내용 |
| --- | --- |
| 1 | 전체 테스트 코드 실행 |
| 2 | Docker 이미지 빌드 및 Docker Hub push |
| 3 | OpenVPN으로 내부망 접속 |
| 4 | 서버 SSH 접속 |
| 5 | 서버에서 Docker image pull |
| 6 | 서버에 `.env` 환경변수 파일 생성 |
| 7 | 기존 컨테이너 교체 배포 |
| 8 | HTTP 응답으로 배포 완료 확인 |

### Required GitHub Secrets

| Secret | 설명 |
| --- | --- |
| `DOCKERHUB_USERNAME` | Docker Hub 사용자명 |
| `DOCKERHUB_TOKEN` | Docker Hub access token |
| `OPENVPN_CONFIG` | 내부망 접속용 `.ovpn` 설정 파일 전체 내용 |
| `SERVER_HOST` | VPN 접속 후 접근 가능한 서버 주소 |
| `SERVER_USER` | SSH 접속 사용자 |
| `SSH_PRIVATE_KEY` | SSH 접속용 private key |
| `GOOGLE_API_KEY` | Google GenAI API key |

### Optional GitHub Secrets

| Secret | 기본값 | 설명 |
| --- | --- | --- |
| `OPENVPN_USERNAME` | 없음 | OpenVPN 사용자명 인증이 필요한 경우 |
| `OPENVPN_PASSWORD` | 없음 | OpenVPN 비밀번호 인증이 필요한 경우 |
| `SSH_KNOWN_HOSTS` | `ssh-keyscan` 사용 | 서버 host key를 고정하고 싶은 경우 |
| `SERVER_PORT` | `22` | SSH 포트 |
| `APP_PORT` | `8080` | 서버에 노출할 애플리케이션 포트 |

## 설정

AI 모델 설정은 `src/main/resources/application.yml`에서 관리합니다.

```yaml
spring:
  ai:
    google:
      genai:
        api-key: ${GOOGLE_API_KEY}
        chat:
          options:
            model: "gemini-2.5-flash"
            temperature: 0.7
```

## AI 응답 원칙

OneVerse의 AI 응답은 다음 기준을 따라야 합니다.

| 원칙 | 설명 |
| --- | --- |
| 하나의 구절 | 사용자의 상황에 어울리는 성경 구절을 하나 선택합니다. |
| 짧은 묵상 | 구절의 의미를 짧고 명확한 묵상 문장으로 전달합니다. |
| 출처 포함 | 응답에는 반드시 성경 구절 출처를 포함합니다. |
| 왜곡 방지 | 성경 본문 자체를 장문으로 대체하거나 임의로 왜곡하지 않습니다. |
| 안전 안내 | 위기, 자해, 학대, 심각한 정신건강 문제에서는 주변 사람, 교회 지도자, 전문기관의 도움을 받도록 안내해야 합니다. |

## 주의 사항

- AI 응답은 성경, 목회자, 교회 공동체를 대체하지 않습니다.
- 추천된 구절과 해석은 사용자가 직접 성경 본문으로 확인해야 합니다.
- 현재 버전은 AI 응답의 정확성을 자동 검증하지 않습니다.
- 상용 서비스로 확장하기 전에는 성경 번역본 라이선스, 개인정보 보호, 상담성 응답의 안전 정책을 반드시 검토해야 합니다.

## 로드맵

| 단계     | 계획                                |
|--------|-----------------------------------|
| 말씀 데이터 | 성경 번역본 및 본문 원본 데이터 연동             |
| 검증     | 추천 구절의 자동 검증 로직 추가                |
| 기록     | 묵상 기록 저장 기능                       |
| 기도     | 기도 제목 관리 기능                       |
| 찬양     | 추천 말씀 및 기도제목에 맞는 찬양 제공            |
| 말씀카드   | 추천 말씀 및 제목에 맞는 생성형 이미지 기반 말씀카드 제공 |
| 개인화 | 사용자별 말씀 히스토리 제공                    |
| 공동체 | 교회 소그룹 또는 공동체 공유 기능                |
| 안전 | 부적절하거나 위험한 입력에 대한 안전 응답 강화         |
| 운영 | 목회자 또는 신학 검토자가 관리할 수 있는 프롬프트 정책 분리 |

## 비전

OneVerse는 사용자가 AI와의 대화에서 끝나는 것이 아니라, 성경을 펴고 하나님 앞에 머무는 시간으로 나아가도록 돕는 서비스를 지향합니다. 기술은 신앙의 중심이 아니라 통로이며, OneVerse의 목적은 더 많은 사람이 일상 속에서 말씀을 기억하고 묵상하도록 돕는 것입니다.

## Thanks To

이 프로젝트를 기획하시고, 인도하시고, 개발하도록 능력주신 주님께 모든 영광을 드립니다.

> 내게 능력주시는 자 안에서 내가 모든 것을 할 수 있느니라.<br>
> \- 빌 4:13

Copyright Jesus Christ. All rights reserved.
