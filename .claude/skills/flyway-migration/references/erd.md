# 상호대차 ERD 요약 (2026-10-20 기준)

원본은 사용자의 노션 ERD 페이지다. 노션이 바뀌면 이 파일도 같이 고친다.
표기: PK, FK, UK, NN(not null), CK(check). 도메인 테이블에는 created_at, updated_at이 공통으로 있다(아래에서 생략).

## 목차
1. 테이블 목록과 도입 단계
2. 테이블 정의
3. DB 제약과 인덱스
4. 의도된 중복
5. 소장도서정보 API 매핑

## 1. 테이블 목록과 도입 단계

| 테이블 | 데이터 출처 | 단계 |
|---|---|---|
| district, material_type, library, library_policy | Flyway 기준 데이터 | 1 |
| cancel_reason | Flyway 기준 데이터 | 2 |
| library_closure | 운영 중 입력 | 1 |
| app_user, staff, patron | 합성 | 1 |
| bib_record | 소장도서정보 API | 1 |
| item | API + 합성(상태, 입수일) | 1 |
| loan_request, loan_request_cancellation, loan_request_history, shipment, loan | 운영 중 생성 | 2 |
| notification_setting, family_group, family_member, family_invitation | 합성, 운영 중 생성 | 2.5 |
| notification | 운영 중 생성 | 3 |
| outbox_event, processed_event | 운영 중 생성 | 4 |
| loan_request_monthly_stats | Batch 집계 | 7 |

Spring Batch 메타 테이블은 7단계에서 Flyway로 추가한다.

## 2. 테이블 정의

### 코드

**district**: code varchar(10) PK(행정구역 코드) · name varchar(20) UK NN

**material_type**: code varchar(20) PK (GENERAL, REFERENCE, RARE, PERIODICAL, CHILDREN) · name varchar(50) NN · interlibrary_allowed boolean NN

**cancel_reason**: code varchar(30) PK (PATRON_CANCEL, NOT_ON_SHELF, DAMAGED, NON_CIRCULATING, SHIP_DEADLINE_EXCEEDED, OTHER) · name varchar(50) NN · patron_message varchar(300) NN · actor_scope varchar(20) NN CK(BORROWER_SIDE, LENDER, SYSTEM) · memo_required boolean NN (OTHER만 true)

### 도서관

**library**: id bigint PK · code varchar(10) UK NN (API 관리코드 MA~MX) · name varchar(100) NN · district_code varchar(10) FK NN · road_address varchar(255) NN · latitude numeric(9,6) NN · longitude numeric(9,6) NN · phone varchar(20) · homepage_url varchar(300) · interlibrary_enabled boolean NN

**library_policy**: library_id bigint PK FK · loan_days int NN CK>0 · max_active_requests int NN CK>0 · ship_deadline_days int NN CK>0 · pickup_deadline_days int NN CK>0 · new_book_block_days int NN CK>=0

**library_closure**: id bigint PK · library_id FK NN · start_date date NN · end_date date NN CK>=start_date · reason varchar(200) NN

### 사람과 계정

**app_user**: id bigint PK · login_id varchar(50) UK NN · password_hash varchar(100) (8단계 전 null 허용) · role varchar(20) NN CK(PATRON, LIBRARIAN, LIBRARY_MANAGER, SYSTEM_ADMIN) · enabled boolean NN · UK(id, role)

**staff**: user_id bigint PK · role varchar(20) NN CK(<> PATRON) · name varchar(50) NN · library_id FK
- 복합 FK (user_id, role) → app_user(id, role)
- CK: role = SYSTEM_ADMIN 이 아니면 library_id not null

**patron**: id bigint PK · library_id FK NN · card_number varchar(30) NN · name varchar(50) NN · email varchar(100) · phone varchar(20) · birth_date date · status varchar(20) NN CK(ACTIVE, SUSPENDED, WITHDRAWN) · suspended_until date · app_user_id FK UK · UK(library_id, card_number)

**notification_setting**: patron_id bigint PK FK · family_alert boolean NN · email_enabled boolean NN · sms_enabled boolean NN

### 책

**bib_record**: id bigint PK · isbn13 char(13) UK NN · title varchar(500) NN · volume varchar(20) · volume_title varchar(200) · author varchar(500) · publisher varchar(200) · published_year smallint · kdc varchar(20)

**item**: id bigint PK · library_id FK NN · bib_record_id FK NN · source_book_key bigint · barcode varchar(30) NN · call_number varchar(100) · shelf_location varchar(100) · material_type_code FK NN · status varchar(20) NN CK(AVAILABLE, ON_LOAN, RESERVED, LENT_OUT, MISSING, DAMAGED, IN_REPAIR, LOST, WITHDRAWN) · interlibrary_lendable boolean NN · acquired_at date · version bigint NN
- UK(library_id, barcode), UK(library_id, source_book_key), UK(id, library_id, bib_record_id)

### 상호대차

**loan_request**: id bigint PK · request_no varchar(20) UK NN · patron_id FK NN · borrowing_library_id FK NN · lending_library_id bigint NN · bib_record_id bigint NN · item_id bigint NN · status varchar(20) NN CK(REQUESTED, SHIPPED, ARRIVED, ON_LOAN, UNCLAIMED, RETURNED, RETURN_SHIPPED, COMPLETED, CANCELLED, LOST) · ship_due_date date NN · pickup_due_date date · version bigint NN
- 복합 FK (item_id, lending_library_id, bib_record_id) → item(id, library_id, bib_record_id)
- CK borrowing_library_id <> lending_library_id
- 신청 시각은 created_at, 종료 시각은 이력에서 구한다

**loan_request_cancellation**: loan_request_id bigint PK FK · cancel_reason_code FK NN · memo varchar(500)
- 취소한 사람과 시각은 이력의 CANCEL 행에 있다. 메모 필수 여부는 서비스가 cancel_reason.memo_required로 검증

**loan_request_history** (추가 전용, updated_at 없음): id bigint PK · loan_request_id FK NN · from_status varchar(20) · to_status varchar(20) NN · action varchar(30) NN CK(REQUEST, SHIP, CANCEL, ARRIVE, CHECK_OUT, EXPIRE_PICKUP, CHECK_IN, REPORT_LOST, RETURN_SHIP, COMPLETE) · actor_type varchar(20) NN CK(PATRON, LIBRARIAN, SYSTEM) · actor_user_id FK · note varchar(500) · occurred_at timestamptz NN
- CK: actor_type = SYSTEM 이면 actor_user_id null, 아니면 not null

**shipment**: id bigint PK · loan_request_id FK NN · direction varchar(10) NN CK(OUTBOUND, RETURN) · tracking_no varchar(50) · shipped_at timestamptz NN · received_at timestamptz CK>=shipped_at · UK(loan_request_id, direction)

**loan**: loan_request_id bigint PK FK · received_by_patron_id FK NN · checked_out_at timestamptz NN · due_date date NN (불변) · returned_at timestamptz

### 가족

**family_group**: id bigint PK

**family_member**: patron_id bigint PK FK · family_group_id FK NN · guardian boolean NN · joined_at timestamptz NN

**family_invitation**: id bigint PK · inviter_patron_id FK NN · invitee_patron_id FK NN CK(<> inviter) · status varchar(20) NN CK(PENDING, ACCEPTED, DECLINED, EXPIRED, CANCELLED) · expires_at timestamptz NN · responded_at timestamptz
- 부분 UK (inviter_patron_id, invitee_patron_id) where status = 'PENDING'

### 알림, 이벤트, 통계

**notification**: id bigint PK · patron_id FK NN · loan_request_id FK · type varchar(30) NN CK(ARRIVED, NOT_SUPPLIED, DUE_SOON, DUE_TODAY, OVERDUE, FAMILY_INVITE) · message varchar(500) NN · source_event_id uuid · read_at timestamptz
- 부분 UK (source_event_id, patron_id) where source_event_id is not null

**outbox_event**: id uuid PK · aggregate_type varchar(50) NN · aggregate_id bigint NN · event_type varchar(50) NN · payload jsonb NN · created_at timestamptz NN · published_at timestamptz

**processed_event**: PK(consumer_group varchar(100), event_id uuid) · processed_at timestamptz NN

**loan_request_monthly_stats**: PK(library_id FK, year_month date) · supplied_count int NN · borrowed_count int NN · not_supplied_count int NN · avg_lead_days numeric(5,2)

## 3. DB 제약과 인덱스

DB가 직접 지키는 규칙 (모두 Flyway SQL로만 표현 가능):

| 대상 | 종류 | 내용 |
|---|---|---|
| loan_request | 부분 UK | item_id where status not in (CANCELLED, COMPLETED, LOST) |
| loan_request | 부분 UK | (patron_id, bib_record_id) where status in (REQUESTED, SHIPPED, ARRIVED, ON_LOAN) |
| loan_request | 복합 FK | (item_id, lending_library_id, bib_record_id) → item |
| loan_request | CK | 요청관 <> 제공관 |
| staff | 복합 FK + CK | 역할이 계정과 같고, 관리자 외에는 도서관 필수 |
| loan_request_history | CK | 시스템이면 행위자 없음 |
| family_invitation | 부분 UK | 대기 중 초대 하나 |
| notification | 부분 UK | 같은 이벤트로 같은 사람에게 한 번 |
| item | UK | (library_id, source_book_key) 재적재 중복 방지 |

조회 인덱스:

| 테이블 | 인덱스 | 용도 |
|---|---|---|
| item | (library_id, bib_record_id, status) | 배정 가능 자료 찾기 |
| loan_request | (lending_library_id, status) | 제공관 대시보드 |
| loan_request | (borrowing_library_id, status) | 요청관 대시보드 |
| loan_request | (patron_id, status) | 내 목록, 동시 진행 한도 |
| loan_request | (status, ship_due_date) | 발송 기한 초과 배치 |
| loan_request | (status, pickup_due_date) | 미수령 배치 |
| loan | (due_date) where returned_at is null | 반납 알림, 연체 |
| loan_request_history | (loan_request_id, occurred_at) | 타임라인 |
| library_closure | (library_id, start_date, end_date) | 중단 기간 확인 |
| notification | (patron_id, created_at) | 알림함 |
| outbox_event | (created_at) where published_at is null | 미발행 폴링 |

## 4. 의도된 중복

| 컬럼 | 이유 | 보장 |
|---|---|---|
| loan_request.lending_library_id, bib_record_id | 대시보드 조회, 중복 신청 부분 UK | 복합 FK |
| staff.role | 역할별 도서관 CK | 복합 FK |
| loan_request.borrowing_library_id | 신청 시점 소속 기록 | 시점 기록 |
| ship_due_date, pickup_due_date | 정책이 바뀌어도 기한 유지 | 시점 기록 |
| loan_request_monthly_stats | 조회 성능 | 배치 재계산 |

## 5. 소장도서정보 API 매핑

API: `https://apis.data.go.kr/7010000/lib/getList` (title, manageCd 필수, type=json). 인증키는 환경변수 `LIBRARY_API_SERVICE_KEY` (디코딩 키). `.env` 파일은 읽지 않는다.

| API 필드 | 컬럼 | 변환 |
|---|---|---|
| manageCode | library.code → item.library_id | 이름(libName)은 표기가 제각각이라 쓰지 않음 |
| bookKey | item.source_book_key | 문자열, 숫자 모두 허용 |
| regNo | item.barcode | 그대로 |
| callNo | item.call_number, bib_record.kdc | 앞자리 분류번호 추출 |
| shelfLocName | item.shelf_location, material_type_code | "[강동]서고-2 (사서에게 문의)"에서 약칭, 괄호 제거. 자료실 이름으로 유형 결정 |
| isbn | bib_record.isbn13 | 10자리는 13자리로. 비면 행 건너뜀 |
| title, vol, volTitle | title, volume, volume_title | 그대로 |
| author, publisher | author, publisher | 그대로 |
| pubYear | published_year | 숫자 변환 실패 시 null |
| appendixYn | 없음 | Y면 행 건너뜀 |
| speciesKey, controlNo, rnum | 없음 | 저장 안 함 |
