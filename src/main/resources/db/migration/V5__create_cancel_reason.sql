-- 상호대차 요청 취소 사유 코드 테이블.
-- 사유별 자료 후속 처리는 상태머신 코드가 담당하고, 여기에는 표시 문구와 선택 범위만 둔다.
create table cancel_reason (
    code            varchar(30)  not null,
    name            varchar(50)  not null,
    patron_message  varchar(300) not null,
    actor_scope     varchar(20)  not null,
    memo_required   boolean      not null,
    constraint pk_cancel_reason primary key (code),
    constraint ck_cancel_reason_actor_scope check (actor_scope in ('BORROWER_SIDE', 'LENDER', 'SYSTEM'))
);
