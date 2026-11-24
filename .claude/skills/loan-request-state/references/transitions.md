# 상호대차 상태 전환표 (2026-11-24 기준)

원본은 사용자의 노션 요구사항 정의서다. 노션이 바뀌면 이 파일도 같이 고친다.

## 1. 관계 (행위자가 이 요청에서 어느 쪽인가)

| 관계 | 판단 기준 |
|---|---|
| REQUESTER | 행위자 이용자 = 요청의 신청자 |
| FAMILY | 행위자 이용자가 신청자와 같은 가족 그룹의 활성 구성원 |
| BORROWING_LIBRARIAN | 행위자가 사서 또는 도서관 관리자이고 소속 도서관 = 요청관 |
| LENDING_LIBRARIAN | 행위자가 사서 또는 도서관 관리자이고 소속 도서관 = 제공관 |
| SYSTEM | 스케줄러, 배치, 컨슈머 |

시스템 관리자는 어떤 전환도 할 수 없다 (BR-19).

## 2. 요청 상태 전환

| 현재 상태 | 행위 | 다음 상태 | 허용 관계 |
|---|---|---|---|
| 시작 | REQUEST (사전 차단 통과, 자료 자동 배정) | REQUESTED | REQUESTER |
| REQUESTED | SHIP | SHIPPED | LENDING_LIBRARIAN |
| REQUESTED | CANCEL (PATRON_CANCEL) | CANCELLED | REQUESTER, BORROWING_LIBRARIAN |
| REQUESTED | CANCEL (NOT_ON_SHELF, DAMAGED, NON_CIRCULATING, OTHER) | CANCELLED | LENDING_LIBRARIAN |
| REQUESTED | CANCEL (SHIP_DEADLINE_EXCEEDED) | CANCELLED | SYSTEM |
| SHIPPED | ARRIVE | ARRIVED | BORROWING_LIBRARIAN |
| ARRIVED | CHECK_OUT (수령자는 본인 또는 가족) | ON_LOAN | BORROWING_LIBRARIAN |
| ARRIVED | EXPIRE_PICKUP | UNCLAIMED | SYSTEM, BORROWING_LIBRARIAN |
| ON_LOAN | CHECK_IN | RETURNED | BORROWING_LIBRARIAN |
| ON_LOAN | REPORT_LOST | LOST | BORROWING_LIBRARIAN |
| RETURNED | RETURN_SHIP | RETURN_SHIPPED | BORROWING_LIBRARIAN |
| UNCLAIMED | RETURN_SHIP | RETURN_SHIPPED | BORROWING_LIBRARIAN |
| RETURN_SHIPPED | COMPLETE (검수 결과 정상/훼손) | COMPLETED | LENDING_LIBRARIAN |

- 종료 상태: CANCELLED, COMPLETED, LOST
- 표에 없는 전환은 모두 거부한다
- 승인, 수락, 거절 단계는 없다 (BR-03). 연장도 없다 (BR-10)
- 책이 이용자 쪽에 있는 구간은 요청관 사서, 서가와 배송 출발점 쪽은 제공관 사서가 처리한다

## 3. 소장 자료 상태 전환

| 현재 상태 | 행위 | 다음 상태 |
|---|---|---|
| 시작 | 자료 등록 | AVAILABLE |
| AVAILABLE | 자관 대출 (합성 데이터 전용, 기능 없음) | ON_LOAN |
| ON_LOAN | 자관 반납 (합성 데이터 전용) | AVAILABLE |
| AVAILABLE | 상호대차 신청으로 배정 | RESERVED |
| RESERVED | 발송 | LENT_OUT |
| RESERVED | 취소: PATRON_CANCEL, SHIP_DEADLINE_EXCEEDED | AVAILABLE |
| RESERVED | 취소: NOT_ON_SHELF | MISSING |
| RESERVED | 취소: DAMAGED | DAMAGED |
| RESERVED | 취소: NON_CIRCULATING | AVAILABLE + interlibrary_lendable = false |
| RESERVED | 취소: OTHER | AVAILABLE |
| LENT_OUT | 완료 검수 정상 | AVAILABLE |
| LENT_OUT | 완료 검수 훼손 | DAMAGED |
| LENT_OUT | 요청 분실 | LOST |
| MISSING | 발견 | AVAILABLE |
| DAMAGED | 수리 보냄 | IN_REPAIR |
| IN_REPAIR | 수리 완료 | AVAILABLE |
| DAMAGED, MISSING, LOST | 제적 | WITHDRAWN |

배정 조건: 자료가 AVAILABLE이고, interlibrary_lendable = true이고, 자료 유형이 상호대차 허용이며, 입수일이 신간 제한 기간을 지났다.

## 4. 취소 사유

| 코드 | 고를 수 있는 쪽 | 메모 | 이용자 안내 문구 |
|---|---|---|---|
| PATRON_CANCEL | BORROWER_SIDE (신청자, 요청관 사서) | 선택 | 신청이 취소되었습니다 |
| NOT_ON_SHELF | LENDER | 선택 | 제공 도서관에서 자료를 찾을 수 없어 신청이 취소되었습니다 |
| DAMAGED | LENDER | 선택 | 자료 상태 문제로 제공이 어려워 신청이 취소되었습니다 |
| NON_CIRCULATING | LENDER | 선택 | 상호대차가 제한된 자료로 확인되어 신청이 취소되었습니다 |
| SHIP_DEADLINE_EXCEEDED | SYSTEM | 없음 | 제공 도서관의 처리 기한이 지나 신청이 취소되었습니다 |
| OTHER | LENDER | 필수 | 제공 도서관 사정으로 신청이 취소되었습니다 |

도서관 간 메모는 이용자에게 보내지 않는다 (BR-06). 발송 불가로 끝난 요청은 다른 도서관으로 넘기지 않고, 이용자에게 다른 소장 도서관을 안내한다 (BR-07).

## 5. 진행 중 기준

| 기준 | 상태 | 쓰는 곳 | DB |
|---|---|---|---|
| 이용자 기준 | REQUESTED, SHIPPED, ARRIVED, ON_LOAN | 같은 서지 중복 신청 금지 (BR-20), 동시 진행 한도 | ux_loan_request_patron_bib_active |
| 자료 기준 | 종료 상태(CANCELLED, COMPLETED, LOST)가 아닌 전부 | 자료 하나에 진행 중 요청 하나 (BR-04) | ux_loan_request_item_active |

## 6. 이용자 표시 단계

| 이용자 표시 | 내부 상태 |
|---|---|
| 발송 준비 중 | REQUESTED |
| 배송 중 | SHIPPED |
| 수령 대기 | ARRIVED |
| 대출 중 | ON_LOAN |
| 반납 완료 | RETURNED, RETURN_SHIPPED, COMPLETED |
| 취소됨 | CANCELLED (사유 PATRON_CANCEL) |
| 제공 불가 | CANCELLED (그 외 사유) |
| 수령 기한 만료 | UNCLAIMED |
| 분실 | LOST |

## 7. 사전 차단 (신청 시점 검증, BR-01, BR-02, BR-20)

하나라도 걸리면 요청을 만들지 않는다.

- 자료: 배정 가능한 자료가 제공관에 없음
- 자료 유형: 상호대차 불허 유형, 신간 제한 기간
- 도서관: 요청관 또는 제공관이 상호대차 미참여, 중단 기간
- 이용자: 연체 중, 정지, 동시 진행 한도 초과, 같은 서지 진행 중
- 자관: 요청관에 대출 가능한 같은 서지가 있음
- 요청관과 제공관이 같음
