---
name: domain-feature
description: 이 프로젝트(상호대차, Spring Boot 4 + JPA)에 도메인 기능을 추가하거나 고칠 때 사용한다. 엔티티, 리포지토리, 서비스, 컨트롤러, 요청/응답 DTO, 예외 처리, 그리고 각 층의 테스트를 만드는 작업이 해당한다. "기능 추가", "엔티티 만들어", "API 만들어", "서비스 구현", "도서관/서지/소장 자료/이용자/가족/알림 기능", "CRUD", "엔드포인트", "테스트 작성" 같은 요청이면 사용자가 구조를 언급하지 않아도 반드시 이 스킬을 따른다. 스키마 변경이 필요하면 flyway-migration, 상태 전환이 관련되면 loan-request-state 스킬도 함께 따른다.
---

# 도메인 기능 추가

이 프로젝트는 기술 학습이 목적이라, 기능 수보다 **구조가 일관되고 각 층이 제대로 테스트되는 것**이 중요하다. 아래 구조와 규칙은 그 일관성을 지키기 위한 것이다.

## 패키지 구조 (제안)

사용자와 패키지 구조를 확정하면 이 절을 갱신한다. 그 전에는 이 구조를 따르되, 처음 적용할 때 사용자에게 제안 구조라고 알린다.

```
com.hyeon.interlibraryloan
├── common          공통 엔티티 기반(생성/수정 시각), 예외, 행위자(Actor), 시간(Clock) 설정
├── library         도서관, 정책, 중단 기간, 코드 테이블
├── book            서지, 소장 자료
├── member          계정, 직원, 이용자
├── loanrequest     상호대차 요청, 이력, 취소, 배송, 대출
├── family          가족 그룹, 구성원, 초대
└── notification    알림 (3단계)
```

기능 패키지 안은 세 층으로 나눈다.

| 층 | 담는 것 | 의존 |
|---|---|---|
| `domain` | 엔티티, 리포지토리 인터페이스, 순수 규칙, 도메인 예외 | 다른 층에 의존하지 않음 |
| `application` | 서비스, 명령/조회 객체, 트랜잭션 | domain |
| `api` | 컨트롤러, 요청/응답 DTO | application |

의존은 `api → application → domain` 한 방향이다. 다른 기능의 데이터가 필요하면 그 기능의 application 서비스나 domain 타입을 쓰고, 기능 간 순환 의존을 만들지 않는다.

## 작업 순서

1. **기준 확인**: `flyway-migration/references/erd.md`의 테이블 정의와, 관련 요구사항 ID(FR-xx, BR-xx)를 확인한다. 정해지지 않은 규칙은 지어내지 말고 사용자에게 묻는다.
2. **마이그레이션**: 스키마가 필요하면 `flyway-migration` 스킬로 먼저 만든다.
3. **엔티티와 도메인 규칙**, 순수 단위 테스트
4. **리포지토리**, 필요한 경우만 리포지토리 테스트
5. **서비스**, 통합 테스트
6. **컨트롤러와 DTO**, 웹 계층 테스트
7. `./gradlew test` 통과 확인
8. 사용자에게 무엇을 만들었고 어떤 요구사항 ID를 다뤘는지, 남은 것은 무엇인지 보고한다

## 엔티티 규칙

| 규칙 | 이유 |
|---|---|
| 기본 생성자는 `protected`, 공개 setter 없음 | 상태 변경을 이름 있는 행위 메서드로만 하게 해서 규칙 위반 경로를 없앤다 |
| 필수 값은 생성자나 정적 팩토리로 받는다 | 반쯤 만들어진 객체가 생기지 않게 한다 |
| 연관은 `@ManyToOne(fetch = LAZY)`, 양방향은 꼭 필요할 때만 | N+1과 순환 참조를 피한다 |
| cascade는 진짜 소유한 자식에만 | 다른 집합의 데이터를 실수로 지우거나 저장하지 않게 한다 |
| 상태는 `@Enumerated(EnumType.STRING)` | 순서 변경에 안전하고 DB CHECK와 값이 같다 |
| ID는 `@SequenceGenerator(allocationSize = 50)`, 시퀀스 이름은 `{table}_seq` | 마이그레이션과 맞추고 일괄 INSERT를 가능하게 한다 |
| 1:0..1 테이블은 `@MapsId` | ERD의 공유 PK 구조와 맞춘다 |
| `version` 컬럼이 있으면 `@Version` | 낙관적 락 |
| 생성/수정 시각은 공통 기반 클래스와 JPA Auditing, 타입은 `Instant` | 모든 테이블에서 같은 방식 |
| Lombok `@Data`, `@Setter`, `@EqualsAndHashCode`는 엔티티에 쓰지 않는다 | 지연 로딩, 해시 컬렉션에서 문제를 만든다. `@Getter`와 `@NoArgsConstructor(access = PROTECTED)`는 괜찮다 |

설정은 `ddl-auto: validate`, `open-in-view: false`를 유지한다.

## 서비스 규칙

- 클래스에 `@Transactional(readOnly = true)`, 쓰기 메서드에만 `@Transactional`.
- 입력은 명령 record와 **행위자(Actor)**를 받는다. Security 도입 전에도 행위자를 파라미터로 받아야 권한 검사를 나중에 덧붙이지 않아도 된다.
- 현재 시각과 날짜는 주입받은 `Clock`으로 구한다. 기한 계산을 테스트에서 고정된 시각으로 검증하기 위해서다.
- 분기가 많은 계산과 규칙은 도메인으로 내려서 단위 테스트한다. 서비스는 조회, 조율, 저장만 한다.
- 엔티티를 컨트롤러로 넘기지 않는다. 응답용 객체로 바꿔서 반환한다.

## API 규칙

| 항목 | 규칙 |
|---|---|
| 경로 | 이용자용 `/api/patron/**`, 사서용 `/api/staff/**`, 관리자용 `/api/admin/**`. 같은 데이터라도 관점이 다르면 엔드포인트를 나눈다 |
| 상태 전환 | 상태 값을 PATCH로 바꾸지 않고 행위 단위 POST로 노출한다. 예) `POST /api/staff/loan-requests/{id}/ship` |
| DTO | Java record, Bean Validation으로 입력 검증 |
| 응답 | 이용자 응답은 내부 상태 대신 이용자 표시 단계를 준다. 제공관 사서에게는 이용자 이름을 마스킹한다. 상세 응답에는 지금 이 행위자가 할 수 있는 행위 목록을 담는다 |
| 오류 | 도메인 예외를 `@RestControllerAdvice`에서 `ProblemDetail`로 바꾼다. 오류 코드는 enum으로 관리한다 |

## 테스트 규칙

| 층 | 방식 | 확인할 것 |
|---|---|---|
| 도메인 | 순수 JUnit 5 + AssertJ, Spring 없음 | 규칙, 행위 메서드, 경계값 |
| 리포지토리 | `@DataJpaTest` + Testcontainers Postgres | 직접 작성한 쿼리, CHECK, 부분 유니크, 복합 FK. 단순 저장/조회만 하는 리포지토리는 테스트하지 않는다 |
| 서비스 | `@SpringBootTest` + Testcontainers | 트랜잭션, 여러 엔티티에 걸친 흐름 |
| 컨트롤러 | `@WebMvcTest` + 서비스 목(`@MockitoBean`) | 입력 검증, 상태 코드, 응답 JSON 모양, 오류 형식 |

- **H2 같은 임베디드 DB는 쓰지 않는다.** 부분 인덱스와 CHECK는 Postgres 동작이라, 다른 DB에서 통과해도 의미가 없다. 슬라이스 테스트가 임베디드 DB로 바꾸지 않도록 공용 Testcontainers 설정을 쓴다.
- `@DisplayName`은 한국어로 쓰고, 관련 규칙 ID를 붙인다. 예) `"BR-20 같은 서지를 진행 중인 이용자는 다시 신청할 수 없다"`
- 본문은 given, when, then 주석으로 나눈다.
- 테스트 데이터는 기능별 픽스처 클래스로 만든다. 큰 합성 데이터에 기대지 않는다. 그래야 테스트끼리 영향을 주지 않는다.
- Spring Boot 4는 테스트 슬라이스와 Jackson 패키지가 이전 버전과 다르다. import는 기억에 의존하지 말고 실제 의존성의 클래스를 확인한다.

## 체크리스트

- [ ] 패키지와 층 의존 방향이 위 구조를 따른다
- [ ] 엔티티에 공개 setter가 없고, 상태 변경은 행위 메서드로만 한다
- [ ] 서비스가 행위자와 Clock을 받는다
- [ ] 컨트롤러가 엔티티를 그대로 반환하지 않는다
- [ ] 각 층의 테스트가 있고 임베디드 DB를 쓰지 않는다
- [ ] `./gradlew test`가 통과한다
- [ ] 다룬 요구사항 ID를 보고했다
