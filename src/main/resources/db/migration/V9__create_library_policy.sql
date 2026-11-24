-- 도서관별 상호대차 정책. 도서관과 1:1이며 도서관 id를 그대로 PK로 쓴다.
create table library_policy (
    library_id            bigint      not null,
    loan_days             int         not null,
    max_active_requests   int         not null,
    ship_deadline_days    int         not null,
    pickup_deadline_days  int         not null,
    new_book_block_days   int         not null,
    created_at            timestamptz not null,
    updated_at            timestamptz not null,
    constraint pk_library_policy primary key (library_id),
    constraint fk_library_policy_library_id foreign key (library_id) references library (id),
    constraint ck_library_policy_loan_days check (loan_days > 0),
    constraint ck_library_policy_max_active_requests check (max_active_requests > 0),
    constraint ck_library_policy_ship_deadline_days check (ship_deadline_days > 0),
    constraint ck_library_policy_pickup_deadline_days check (pickup_deadline_days > 0),
    constraint ck_library_policy_new_book_block_days check (new_book_block_days >= 0)
);
