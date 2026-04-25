# HireFlow 🎯
> AI 기반 채용 공고 매칭 및 지원 현황 관리 비서

<br>
## 프로젝트 소개

취업 준비생이 채용 공고를 직접 찾아다니며 스프레드시트로 관리하는 불편함을 해소하기 위해 만든 서비스입니다.  
공고 크롤링 → AI 태그 추출 → 내 기술스택 기반 매칭 → 지원 현황 관리 → 마감/면접 알림까지 한 곳에서 처리합니다.

<br>
## 주요 기능

- **공고 자동 수집** : Jsoup + @Scheduled로 주기적 크롤링 (Wanted, 사람인)
- **AI 태그 추출** : OpenAI API로 공고에서 기술스택 자동 추출
- **공고 추천** : 내 기술스택 기반 Top 3 매칭 (Redis 캐싱)
- **이력서 파싱** : PDF 업로드 → AI 파싱 → 기술스택 자동 업데이트 (비동기)
- **지원 현황 관리** : 지원 등록 / 상태 변경 / 메모 / 면접 날짜 관리
- **알림** : 마감 D-3 / 면접 D-1 / 파싱 완료 이메일 알림 (JavaMailSender)
  <br>
## 기술 스택

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=spring-security&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![AWS](https://img.shields.io/badge/AWS-232F3E?style=for-the-badge&logo=amazon-aws&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=github-actions&logoColor=white)
![Swagger](https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)

| 분류 | 기술 |
|---|---|
| Backend | Spring Boot 3.x, Spring Security, JWT |
| Database | PostgreSQL, JPA/Hibernate |
| Cache | Redis |
| Crawling | Jsoup, @Scheduled |
| AI | OpenAI API |
| File | AWS S3 |
| Notify | JavaMailSender |
| Infra | Docker, AWS EC2, GitHub Actions |
| Docs | Swagger (springdoc-openapi) |

<br>
## 아키텍처

```
[Scheduler]
    └── JobCrawlerService (Jsoup)
            └── OpenAI API (tech_stack_tags 추출)
                    └── job_postings 저장
 
[Client]
    └── Spring Security + JWT Filter
            └── Controller → Service → Repository
                    ├── Redis (공고 목록 캐싱)
                    ├── AWS S3 (이력서 PDF)
                    ├── OpenAI API (이력서 파싱)
                    └── JavaMailSender (알림)
```

<br>
## ERD

| 테이블 | 설명 |
|---|---|
| users | 회원 정보, 기술스택, 이력서 파싱 상태 |
| job_postings | 크롤링/직접등록 공고, AI 추출 태그 |
| applications | 지원 현황, 상태, 면접 날짜 |
| cover_letters | 자소서 작성 및 AI 첨삭 |
| notifications | 마감/면접/파싱 완료 알림 |

<br>
## 패키지 구조

```
com.hireflow.hireflow
├── domain
│   ├── auth
│   ├── user
│   ├── jobposting
│   ├── application
│   ├── notification
│   └── coverletter
├── global
│   ├── common
│   ├── config
│   ├── exception
│   └── security
└── infra
    ├── ai
    ├── crawler
    ├── mail
    └── s3
```

<br>
## API 명세

Swagger : `http://localhost:8080/swagger-ui/index.html`

| 분류 | 엔드포인트 수 |
|---|---|
| Auth | 4 |
| User | 5 |
| JobPosting | 6 |
| Application | 7 |
| CoverLetter | 4 |
| Notification | 3 |

<br>
## 실행 방법

```bash
# 1. 환경변수 설정 (.env 또는 application-local.yml)
DB_URL=jdbc:postgresql://localhost:5432/hireflow
DB_USERNAME=postgres
DB_PASSWORD=본인비번
OPENAI_API_KEY=sk-...
AWS_ACCESS_KEY=...
AWS_SECRET_KEY=...
MAIL_USERNAME=...
MAIL_PASSWORD=...
 
# 2. 실행
./gradlew bootRun
```

<br>
## 개발 기간 및 일정

| 기간 | 내용 |
|---|---|
| 1주차 | 기반 구축 (DB, Entity, JWT, 크롤러, 공고 API, Redis) |
| 2주차 | 핵심 기능 (지원 관리, S3, AI 파싱, 추천, 알림) |
| 3주차 | 배포 (Docker, EC2, CI/CD, Swagger, README) |

<br>
## 트러블슈팅

> 구현하면서 겪은 문제들을 여기에 정리할 예정입니다.

<br>
## Git Convention

### Commit Message

| Type | 사용 시점 | 예시 |
|---|---|---|
| feat | 새로운 기능 추가 | `feat: add user login` |
| fix | 버그 수정 | `fix: resolve password validation error` |
| docs | 문서 수정 | `docs: update README` |
| style | 코드 스타일 변경 | `style: apply code formatting` |
| refactor | 코드 리팩토링 | `refactor: improve auth logic` |
| test | 테스트 코드 추가/수정 | `test: add login unit test` |
| perf | 성능 개선 | `perf: optimize DB query` |
| build | 빌드 파일 수정 | `build: update gradle dependencies` |
| ci | CI/CD 설정 수정 | `ci: add GitHub Actions workflow` |
| chore | 기타 작업 | `chore: update dependencies` |
| add | 파일/라이브러리 추가 | `add: add swagger dependency` |

### Branch

| Branch | 설명 |
|---|---|
| `main` | 배포 브랜치 |
| `develop` | 개발 통합 브랜치 |
| `feat/기능명` | 기능 개발 브랜치 |
| `fix/버그명` | 버그 수정 브랜치 |

<br>
---

> 👩‍💻 개발자 : 이주희 · [GitHub](https://github.com/juheehee) · [Blog](https://bloggerddori.tistory.com/)