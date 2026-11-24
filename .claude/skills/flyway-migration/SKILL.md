---
name: flyway-migration
description: 이 프로젝트(상호대차)의 DB 스키마를 바꾸는 모든 작업에 사용한다. 테이블/컬럼/인덱스/제약 추가·변경, 코드 테이블이나 도서관 같은 기준 데이터(seed) 추가, 엔티티 추가로 스키마가 필요해진 경우, "마이그레이션", "Flyway", "테이블 만들어", "컬럼 추가", "ERD 반영", "DDL", "schema", "migration" 같은 요청이면 사용자가 Flyway를 직접 언급하지 않아도 반드시 이 스킬을 따른다. ddl-auto로 스키마를 만들려는 유혹이 들 때도 이 스킬을 본다.
---

# Flyway 마이그레이션 추가

이 프로젝트는 스키마를 **Flyway SQL로만** 관리하고 `spring.jpa.hibernate.ddl-auto`는 `validate`로 고정한다. 운영 환경에서 쓰는 방식을 익히는 것이 프로젝트 목적 중 하나이고, 부분 유니크 인덱스, CHECK, 복합 외래키처럼 JPA로 표현할 수 없는 규칙을 DB가 직접 지키게 하려는 것이다. 엔티티와 SQL이 어긋나면 앱이 기동하지 않는 것이 정상 동작이다.

테이블 정의의 기준은 `references/erd.md`다. 사용자가 노션 ERD를 바꿨다고 하면 그 파일부터 갱신한다.

## 1. 파일 위치와 이름

- 위치: `src/main/resources/db/migration/`
- 이름: `V{번호}__{동사}_{대상}.sql` 예) `V3__create_item.sql`, `V7__seed_libraries.sql`, `V12__add_item_shelf_location.sql`
- 번호는 기존 파일 중 가장 큰 번호 + 1. 작성 전에 폴더를 확인한다.
- 한 파일에는 한 가지 관심사만 담는다. 테이블 하나 생성, 기준 데이터 한 묶음, 변경 하나.

## 2. 이미 적용된 파일은 고치지 않는다

Flyway는 적용된 파일의 체크섬을 기록하므로, 고치면 다른 환경에서 기동이 실패한다. 변경은 항상 새 버전 파일로 한다(`ALTER`, 인덱스 재생성, 데이터 보정).

예외는 하나다. **아직 커밋하지 않았고 로컬에서만 적용된 파일**은 고쳐도 된다. 이때는 사용자에게 로컬 DB(`interlibraryloan`)를 다시 만들어야 한다고 알려준다. Testcontainers 테스트는 매번 새 DB라 영향이 없다.

## 3. 작성 규칙

| 항목 | 규칙 |
|---|---|
| 이름 | snake_case. 외래키 컬럼은 `참조테이블_id`, 코드 참조는 `참조테이블_code` |
| PK | `bigint`. 시퀀스 `{table}_seq`를 `increment by 50`으로 만들고 엔티티의 `@SequenceGenerator(allocationSize = 50)`과 맞춘다. IDENTITY는 일괄 INSERT를 막으므로 쓰지 않는다 |
| 1:0..1 테이블 | 부모 PK를 그대로 PK이자 FK로 쓴다. 시퀀스 없음 |
| 코드 테이블 | `code varchar` PK. 시퀀스 없음 |
| 시각 | `timestamptz`. 날짜만이면 `date` |
| 공통 컬럼 | 도메인 테이블은 `created_at`, `updated_at` (`timestamptz not null`). 추가만 하는 테이블은 `created_at` 또는 `occurred_at`만 |
| 상태, 역할 | `varchar` + CHECK. Postgres enum 타입은 쓰지 않는다. 값 추가가 마이그레이션 하나로 끝나기 때문이다 |
| boolean | `is_` 접두어 없이 |
| 낙관적 락 | ERD에 `version`이 있으면 `bigint not null default 0` |

**제약 이름은 반드시 붙인다.** 나중에 `ALTER`로 지우거나 바꿀 때 이름이 필요하다.

| 종류 | 이름 |
|---|---|
| PK | `pk_{table}` |
| FK | `fk_{table}_{column}` (복합이면 대표 이름) |
| UNIQUE | `uk_{table}_{columns}` |
| 부분 UNIQUE 인덱스 | `ux_{table}_{의미}` |
| CHECK | `ck_{table}_{column 또는 의미}` |
| 일반 인덱스 | `ix_{table}_{columns}` |

**외래키에는 인덱스를 직접 만든다.** PostgreSQL은 FK에 인덱스를 자동으로 만들지 않는다. 복합 인덱스의 첫 컬럼이 그 FK라면 따로 만들지 않아도 된다.

## 4. 예시

```sql
create sequence item_seq increment by 50;

create table item (
    id                 bigint       not null,
    library_id         bigint       not null,
    bib_record_id      bigint       not null,
    barcode            varchar(30)  not null,
    material_type_code varchar(20)  not null,
    status             varchar(20)  not null,
    interlibrary_lendable       boolean      not null,
    version            bigint       not null default 0,
    created_at         timestamptz  not null,
    updated_at         timestamptz  not null,
    constraint pk_item primary key (id),
    constraint fk_item_library_id foreign key (library_id) references library (id),
    constraint fk_item_bib_record_id foreign key (bib_record_id) references bib_record (id),
    constraint fk_item_material_type_code foreign key (material_type_code) references material_type (code),
    constraint uk_item_library_id_barcode unique (library_id, barcode),
    constraint uk_item_id_library_id_bib_record_id unique (id, library_id, bib_record_id),
    constraint ck_item_status check (status in ('AVAILABLE','ON_LOAN','RESERVED','LENT_OUT','MISSING','DAMAGED','IN_REPAIR','LOST','WITHDRAWN'))
);

create index ix_item_library_id_bib_record_id_status on item (library_id, bib_record_id, status);
create index ix_item_bib_record_id on item (bib_record_id);
```

부분 유니크 인덱스와 복합 외래키는 이 프로젝트의 핵심 규칙이라 형식을 지킨다.

```sql
-- 자료 기준 진행 중: 자료 하나에 진행 중 요청은 하나 (BR-04)
create unique index ux_loan_request_item_active
    on loan_request (item_id)
    where status not in ('CANCELLED','COMPLETED','LOST');

-- 이용자 기준 진행 중: 같은 서지 중복 신청 금지 (BR-20)
create unique index ux_loan_request_patron_bib_active
    on loan_request (patron_id, bib_record_id)
    where status in ('REQUESTED','SHIPPED','ARRIVED','ON_LOAN');

-- 제공관과 서지가 배정 자료와 어긋나지 않게
constraint fk_loan_request_item foreign key (item_id, lending_library_id, bib_record_id)
    references item (id, library_id, bib_record_id)
```

상태 값 목록이 바뀌면 CHECK 제약과 위 부분 인덱스의 상태 목록도 함께 바뀌어야 한다. 이 경우 `loan-request-state` 스킬의 "상태 목록이 바뀔 때" 절차를 함께 따른다.

## 5. 기준 데이터(seed)

모든 환경에 똑같이 있어야 하는 데이터만 Flyway로 넣는다. 자치구, 자료 유형, 취소 사유, 도서관 22곳, 도서관 기본 정책이 해당한다. 이용자, 계정, 자료 상태처럼 개발과 시연에만 필요한 데이터는 합성 데이터 생성기의 몫이라 마이그레이션에 넣지 않는다.

- 기준 데이터 파일은 테이블 생성 파일과 분리한다. 예) `V5__create_library.sql`, `V6__seed_libraries.sql`
- 시퀀스를 쓰는 테이블은 `nextval('{table}_seq')`로 ID를 만든다.
- 도서관 행은 API 관리 코드(MA~MX)를 `code`로 넣는다. 주소, 좌표, 자치구는 사용자가 준 값을 쓰고, 모르는 값을 지어내지 않는다. 비어 있으면 사용자에게 묻는다.

## 6. 엔티티와 맞추기

마이그레이션과 같은 작업 안에서 엔티티를 함께 맞춘다. `validate`가 잡아내는 흔한 불일치는 이렇다.

- `varchar` 길이와 `@Column(length)`, `numeric(9,6)`과 `precision/scale`
- 상태 컬럼은 `@Enumerated(EnumType.STRING)`
- 시퀀스 이름과 `allocationSize`
- `timestamptz`는 `Instant` 또는 `OffsetDateTime`, `date`는 `LocalDate`

엔티티 작성 규칙은 `domain-feature` 스킬을 따른다.

## 7. 검증

1. 테스트를 돌린다. Testcontainers Postgres에 전체 마이그레이션이 적용되고 엔티티 검증까지 통과해야 한다.
   ```bash
   ./gradlew test
   ```
2. CHECK, 부분 유니크, 복합 FK를 새로 만들었다면 그 규칙을 깨는 INSERT가 실패하는지 리포지토리 테스트로 확인한다. 이런 규칙은 코드 리뷰로는 놓치기 쉽다.
3. 실패하면 오류 메시지의 테이블과 컬럼을 보고 SQL과 엔티티 중 어느 쪽이 ERD와 다른지 확인한다. ERD가 틀렸다고 판단되면 고치기 전에 사용자에게 알린다.

## 체크리스트

- [ ] 번호가 기존 최대 + 1이다
- [ ] 적용된 파일을 수정하지 않았다
- [ ] 모든 제약에 이름이 있다
- [ ] 모든 FK에 인덱스가 있다
- [ ] 상태 값 CHECK와 부분 인덱스의 상태 목록이 Java enum과 같다
- [ ] 엔티티가 함께 맞춰졌다
- [ ] `./gradlew test`가 통과한다
