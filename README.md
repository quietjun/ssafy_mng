# 📋 SSAFY 교육생 및 과제 관리 시스템 (SSAFY MNG) 프로젝트 명세서

> **프로젝트명**: SSAFY MNG (SSAFY Management & AI Code Review System)  
> **버전**: v1.0  
> **작성 일자**: 2026-09-01  

---

## 1. 프로젝트 개요 (Overview)

본 시스템은 **SSAFY(삼성청년SW아카데미) 교육 과정 관리 및 알고리즘 과제/워크샵 제출·AI 자동 분석 시스템**입니다.  
교육생들의 일자별 과제 제출 현황 관리, 캡처 이미지 및 소스코드 기반의 **Google Gemini AI 2단계 검수 및 코드 분석**, 실시간 제출 통계 확인, 학생 성적/시험 관리, 페어 프로그래밍 이력 관리 및 DB 동기화(백업) 기능을 제공합니다.

---

## 2. 사용 기술 스택 (Tech Stack)

### 2.1 Backend (Java / Spring Boot)
- **Language**: Java 21
- **Framework**: Spring Boot 3.4.1
- **Security**: Spring Security (Role-based Authorization: `ROLE_ADMIN`, `ROLE_STUDENT`)
- **Data Access**: Spring Data JPA, Hibernate, MySQL Connector/J
- **AI Integration**: Spring AI (Google GenAI / Gemini 3.5 Flash)
- **HTML Parsing**: JSoup 1.18.3
- **Build Tool**: Apache Maven (`mvnw`)

### 2.2 Frontend (Vue 3 Single Page Application)
- **Framework**: Vue 3 (Composition API / `<script setup>`)
- **Language**: TypeScript
- **State Management**: Pinia 4.0
- **Routing**: Vue Router 5.3
- **HTTP Client**: Axios
- **Syntax Highlighting**: Highlight.js (Java Code Highlighting)
- **Build Tool**: Vite 8.2 & Vue-TSC

### 2.3 Database & Infrastructure
- **Database**: MySQL 8.x
- **OS Support**: Windows / macOS / Linux Cross-Platform 지원
- **Backup & Sync**: MySQL Native CLI (`mysqldump`, `mysql`) 자동 탐색 기반 백업 엔진

---

## 3. 실행 및 개발 환경 (System Environment)

| 구분 | 요구 사양 |
| :--- | :--- |
| **JDK** | Java Development Kit 21 이상 |
| **Node.js** | Node.js v18.0.0 이상 (`npm` 포함) |
| **Database** | MySQL 8.0 이상 (Local DB: `ssafy_db`) |
| **DBMS CLI Tools** | `mysqldump` 및 `mysql` (Windows/Mac 설치 또는 System PATH 등록) |
| **API Keys** | Google Gemini API Key (`GEMINI_API_KEY` 환경변수) |

---

## 4. 핵심 요구사항 및 기능 명세 (Functional Requirements)

### 4.1 과제 및 워크샵 관리 (Problem Management)
- **일자별 문제 등록 (관리자)**:
  - 문제 날짜(오늘 기준 ±3일 또는 지정일), 구분(📘 과제 / 🛠️ 워크샵), 출처 사이트(SWEA, 백준, 프로그래머스 등), 문제명, 추가 설명 등록.
- **기간별 문제 조회**:
  - 관리자: 오늘 기준 ±3일 (총 7일간) 등록 문제 그룹화 조회.
  - 학생: 최근 1주일 문제 목록 및 본인 제출 완료/미제출 상태 확인.
- **문제 수정 및 삭제**:
  - 관리자의 문제 수정 기능 및 제출 이력 연동 삭제 confirmation 기능.

---

### 4.2 2단계 AI 소스코드 제출 및 정밀 검수 (2-Step Submission & AI Inspection)

#### 1단계: 소스코드 및 채점 결과 입력
- **코드 입력**: 직접 붙여넣기 또는 Java 소스 파일(`.java`) 첨부.
- **채점 결과 입력 방식**:
  1. **이미지 캡처 붙여넣기**: 웹페이지상에서 `Ctrl+V`로 채점 이미지 바로 첨부.
  2. **수동 입력**: 실행 시간(ms), 메모리 사용량(KB), 결과 상태(Pass/Fail/TimeLimit) 직접 입력.
- **AI 1단계 분석 요청**: Gemini AI가 소스코드와 채점 이미지를 통합 분석.

#### 2단계: AI 검수 데이터 확인 및 최종 제출
- **AI 추출 데이터 검증**:
  - 결과 상태, 실행 시간, 메모리 사용량, 코드 길이 자동 추출 및 수정 가능.
  - **시간 복잡도 / 공간 복잡도 분석**: AI가 분석한 복잡도 표출.
  - **풀이 핵심 아이디어 & 피드백**: 코드 풀이 전략 요약.
  - **핵심 알고리즘 키워드**: 최대 10개 태그 추출 (추가/삭제 수정 가능).
- **최종 제출**: 최종 데이터를 DB에 저장하고 제출 상태 갱신.

---

### 4.3 학생 제출 풀이 현황 및 피어 리뷰 (Peer Solutions & Status Table)

- **실시간 제출 통계 (관리자)**:
  - 총 학생 수(23명) 대비 제출 인원, 미제출 인원, Pass/Fail 수치 실시간 대시보드.
  - 미제출 학생 명단 실시간 칩(Chip) 표출.
- **학생 제출 현황 테이블 (Interactive Accordion Table)**:
  - **기본 컬럼**: `제출자`, `제출시각`, `실행시간(ms)`, `메모리(KB)`, `길이(B)`, `더보기`.
  - **더보기 토글 (아코디언)**:
    - 클릭 시 단일 선택 상태(Single-Expand)로 하단 확장 패널이 펼침/접힘.
    - 확장 패널 표출 정보: **결과 상태(Pass/Fail)**, **시간 복잡도**, **핵심 알고리즘 키워드(#태그)**, **`🔍 코드 보기` 버튼**.
  - **풀이 코드 Fullscreen 모달**:
    - Highlight.js 기반 줄 번호 및 자바 구문 강조(Syntax Highlighting).
    - 상단 AI 요약 접기/펼치기 및 클립보드 전체 복사 기능.

---

### 4.4 데이터베이스 백업 및 클라우드 동기화 (DB Backup & Sync)

- **클라우드 ➡️ 로컬 DB 자동 동기화**:
  - 원격 Cloud DB에서 `mysqldump`를 실행하여 덤프 파일을 임시 생성 후 로컬 DB로 자동 임포트 및 복원.
  - 복원 완료 후 테이블별 레코드 수(`students`, `problems`, `submissions` 등) 통계 검증.
- **크로스 플랫폼 바이너리 자동 탐색 엔진**:
  - macOS (`/opt/homebrew/bin/mysqldump`), Windows (`C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqldump.exe` 등) 및 `PATH` 환경변수를 자동 탐색하여 OS 독립적 동작 보장.
  - Custom Property (`app.backup.mysqldump-path`, `app.backup.mysql-path`) 수동 오버라이드 지원.

---

### 4.5 학생 / 성적 및 기타 관리 기능

- **학생 정보 관리**: 학번, 이름, 역할 기반 접근 제어.
- **시험 및 성적 관리**: 시험 일정 생성 및 과목별 점수 등록/조회.
- **페어 프로그래밍 히스토리**: 교육생 간 페어 매칭 이력 저장 및 발표자 추첨.
- **관리자 튜터 스크립트**: 자주 사용하는 그리드 데이터 추출 스크립트 모음 제공.

---

## 5. 빌드 및 실행 방법 (Build & Run)

### Backend (Spring Boot)
```bash
./mvnw clean package
java -jar target/ssafymng-0.0.1-SNAPSHOT.jar
```

### Frontend (Vue 3 Dev & Build)
```bash
cd frontend
npm install
npm run dev     # 개발 서버 실행
npm run build   # 프로덕션 정적 빌드 (src/main/resources/static 에 출력)
```
