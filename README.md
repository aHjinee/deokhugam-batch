# deokhugam-batch

도서의 리뷰와 감상을 나눌 수 있는 커뮤니티 플랫폼 **덕후감**의 배치 전용 애플리케이션입니다.
PostgreSQL의 원본 데이터를 주기적으로 집계해 랭킹·통계를 만들고 MongoDB에 저장하며,
만료된 알림·탈퇴 회원을 정리합니다.

## 기술 스택

- Java 17, Spring Boot 3.5.5, Spring Batch
- PostgreSQL (원본 데이터), MongoDB (집계 결과)
- Micrometer + Actuator + Spring Boot Admin (모니터링)
- Gradle, Docker

## 아키텍처

```
PostgreSQL (원본)                 배치 잡                    MongoDB (집계 결과)
─────────────────                ───────                    ──────────────────
users, books, reviews,   ──▶   Spring Batch Job    ──▶     popular_books
comments, review_likes,         (스케줄러 트리거)            popular_reviews
notifications                                               power_users
                                                            user_activity_stats
```

- 원본 트랜잭션 데이터는 PostgreSQL, 대시보드용 읽기 최적화 결과는 MongoDB에 분리 저장합니다.
- 집계 쿼리는 `JdbcTemplate` 네이티브 SQL, 알림·회원 삭제는 JPA Repository를 사용합니다.
- `spring.batch.job.enabled: false`로 기동 시 자동 실행을 막고, `@Scheduled` 스케줄러로만 잡을 트리거합니다.

## 배치 잡

`BatchScheduler`가 아래 잡을 KST 기준으로 실행합니다.

| 잡 | 스케줄(KST) | 설명 |
|---|---|---|
| `notificationDeleteJob` | 매일 00:00 | 확정된 지 7일 지난 알림 삭제 |
| `rankingJob` | 매일 01:00 | 인기 리뷰 → 인기 도서 → 파워 유저 랭킹 집계 (3 스텝) |
| `userActivityStatsJob` | 5분마다 | 당일 사용자 활동 통계 집계 |
| `userDeleteJob` | 매일 02:00 | soft-delete 후 1일 지난 회원 hard delete |

### rankingJob 세부

순서대로 실행되는 3개 스텝으로 구성됩니다.

1. **popularReviewStep** — 기간별(일/주/월/전체) 인기 리뷰 상위 20개 집계
   `점수 = 좋아요 × 0.3 + 댓글 × 0.7`, 상위 10개 리뷰 작성자에게 선정 알림 발송
2. **popularBookStep** — 기간별 인기 도서 상위 10개 집계
   `점수 = 리뷰 수 × 0.4 + 평균 평점 × 0.6`
3. **powerUserStep** — 기간별 파워 유저 상위 10명 집계
   `점수 = 리뷰 점수 × 0.5 + 받은 좋아요 × 0.2 + 받은 댓글 × 0.3`

기간(`PeriodType`): `DAILY`, `WEEKLY`, `MONTHLY`, `ALL_TIME`

## 모니터링

- Actuator 엔드포인트: `/actuator/health`, `/actuator/metrics`, `/actuator/info` 등
- 커스텀 메트릭
  - `deokhugam.batch.job.execution` — 잡별 실행 횟수·시간 (`status` 태그로 COMPLETED/FAILED 구분)
  - `deokhugam.batch.items.processed` — 잡별 누적 처리 건수 (`task`, `period` 태그)

## 프로젝트 구조

```
src/main/java/com/sbproject/deokhugam
├── Application.java              # @EnableScheduling 진입점
├── config/                       # JPA Auditing, QueryDSL 설정
├── domain/                       # 도메인별 엔티티·리포지토리·문서·서비스
│   ├── book, comments, review, user
│   ├── notification              # 알림 (JPA)
│   └── dashboard                 # 집계 결과 (MongoDB Document)
├── job/                          # 배치 잡·스텝·Tasklet
│   ├── ranking                   # rankingJob (3스텝 조합)
│   ├── popularreview, popularbook, poweruser
│   ├── useractivitystats
│   ├── notificationdelete, userdelete
├── monitoring/                   # BatchMetrics, JobExecutionListener
└── scheduler/                    # BatchScheduler (@Scheduled)
```
