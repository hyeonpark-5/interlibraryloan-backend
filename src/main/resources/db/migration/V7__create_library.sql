-- 상호대차 참여 도서관. code는 소장도서정보 API의 도서관 관리 코드(MA~MX)다.
create sequence library_seq increment by 50;

create table library (
    id                    bigint        not null,
    code                  varchar(10)   not null,
    name                  varchar(100)  not null,
    district_code         varchar(10)   not null,
    road_address          varchar(255)  not null,
    latitude              numeric(9, 6) not null,
    longitude             numeric(9, 6) not null,
    phone                 varchar(20),
    homepage_url          varchar(300),
    interlibrary_enabled  boolean       not null,
    created_at            timestamptz   not null,
    updated_at            timestamptz   not null,
    constraint pk_library primary key (id),
    constraint uk_library_code unique (code),
    constraint fk_library_district_code foreign key (district_code) references district (code),
    constraint ck_library_latitude check (latitude between -90 and 90),
    constraint ck_library_longitude check (longitude between -180 and 180)
);

create index ix_library_district_code on library (district_code);
