-- 자치구 코드 테이블. code는 행정표준 시군구 코드다.
create table district (
    code varchar(10) not null,
    name varchar(20) not null,
    constraint pk_district primary key (code),
    constraint uk_district_name unique (name)
);
