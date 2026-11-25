-- 직원(사서, 도서관 관리자, 시스템 관리자). 계정과 1:1이며 계정 id를 PK로 쓴다.
create table staff (
    user_id     bigint       not null,
    role        varchar(20)  not null,
    name        varchar(50)  not null,
    library_id  bigint,
    created_at  timestamptz  not null,
    updated_at  timestamptz  not null,
    constraint pk_staff primary key (user_id),
    -- 직원 역할은 계정 역할과 항상 같다. 계정 역할이 바뀌면 함께 바뀐다
    constraint fk_staff_app_user foreign key (user_id, role)
        references app_user (id, role) on update cascade,
    constraint fk_staff_library_id foreign key (library_id) references library (id),
    constraint ck_staff_role check (role in ('LIBRARIAN', 'LIBRARY_MANAGER', 'SYSTEM_ADMIN')),
    -- 사서와 도서관 관리자는 소속 도서관이 있고, 시스템 관리자는 없다
    constraint ck_staff_library_by_role check (
        (role = 'SYSTEM_ADMIN' and library_id is null)
        or (role <> 'SYSTEM_ADMIN' and library_id is not null))
);

create index ix_staff_library_id on staff (library_id);
