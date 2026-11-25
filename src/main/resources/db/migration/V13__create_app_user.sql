-- 로그인 계정. 비밀번호와 로그인은 Spring Security 단계에서 붙인다.
create sequence app_user_seq increment by 50;

create table app_user (
    id             bigint       not null,
    login_id       varchar(50)  not null,
    password_hash  varchar(100),
    role           varchar(20)  not null,
    enabled        boolean      not null,
    created_at     timestamptz  not null,
    updated_at     timestamptz  not null,
    constraint pk_app_user primary key (id),
    constraint uk_app_user_login_id unique (login_id),
    -- staff가 (계정, 역할)을 한 번에 참조하는 복합 외래키의 대상
    constraint uk_app_user_id_role unique (id, role),
    constraint ck_app_user_role check (role in ('PATRON', 'LIBRARIAN', 'LIBRARY_MANAGER', 'SYSTEM_ADMIN'))
);
