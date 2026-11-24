-- 자료 유형 코드 테이블. 적재할 때 소장 자료실 이름으로 유형을 정한다.
create table material_type (
    code                  varchar(20) not null,
    name                  varchar(50) not null,
    interlibrary_allowed  boolean     not null,
    constraint pk_material_type primary key (code)
);
