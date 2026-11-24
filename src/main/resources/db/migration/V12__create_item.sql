-- 소장 자료. 특정 도서관이 가진 실물 한 권이다.
create sequence item_seq increment by 50;

create table item (
    id                     bigint       not null,
    library_id             bigint       not null,
    bib_record_id          bigint       not null,
    source_book_key        bigint,
    barcode                varchar(30)  not null,
    call_number            varchar(100),
    shelf_location         varchar(100),
    material_type_code     varchar(20)  not null,
    status                 varchar(20)  not null,
    interlibrary_lendable  boolean      not null,
    acquired_at            date,
    version                bigint       not null default 0,
    created_at             timestamptz  not null,
    updated_at             timestamptz  not null,
    constraint pk_item primary key (id),
    constraint fk_item_library_id foreign key (library_id) references library (id),
    constraint fk_item_bib_record_id foreign key (bib_record_id) references bib_record (id),
    constraint fk_item_material_type_code foreign key (material_type_code) references material_type (code),
    -- 등록번호는 도서관 안에서만 유일하다
    constraint uk_item_library_id_barcode unique (library_id, barcode),
    -- 재적재할 때 같은 자료를 다시 넣지 않는다
    constraint uk_item_library_id_source_book_key unique (library_id, source_book_key),
    -- 상호대차 요청이 (자료, 제공관, 서지)를 한 번에 참조하는 복합 외래키의 대상
    constraint uk_item_id_library_id_bib_record_id unique (id, library_id, bib_record_id),
    constraint ck_item_status check (status in (
        'AVAILABLE', 'ON_LOAN', 'RESERVED', 'LENT_OUT', 'MISSING', 'DAMAGED', 'IN_REPAIR', 'LOST', 'WITHDRAWN'))
);

-- 신청 시 배정 가능한 자료 찾기. 첫 컬럼이 library_id라 library 외래키 인덱스를 겸한다.
create index ix_item_library_id_bib_record_id_status on item (library_id, bib_record_id, status);
create index ix_item_bib_record_id on item (bib_record_id);
create index ix_item_material_type_code on item (material_type_code);
