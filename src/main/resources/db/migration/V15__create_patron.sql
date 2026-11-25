-- 이용자. 한 도서관에 소속된 회원이다. 온라인 가입 전에는 계정이 없을 수 있다.
create sequence patron_seq increment by 50;

create table patron (
    id               bigint       not null,
    library_id       bigint       not null,
    card_number      varchar(30)  not null,
    name             varchar(50)  not null,
    email            varchar(100),
    phone            varchar(20),
    birth_date       date,
    status           varchar(20)  not null,
    suspended_until  date,
    app_user_id      bigint,
    created_at       timestamptz  not null,
    updated_at       timestamptz  not null,
    constraint pk_patron primary key (id),
    constraint fk_patron_library_id foreign key (library_id) references library (id),
    constraint fk_patron_app_user_id foreign key (app_user_id) references app_user (id),
    -- 회원번호는 도서관 안에서만 유일하다. 첫 컬럼이 library_id라 외래키 인덱스를 겸한다
    constraint uk_patron_library_id_card_number unique (library_id, card_number),
    constraint uk_patron_app_user_id unique (app_user_id),
    constraint ck_patron_status check (status in ('ACTIVE', 'SUSPENDED', 'WITHDRAWN'))
);
