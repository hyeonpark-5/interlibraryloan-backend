-- 서지. 같은 책은 ISBN-13 하나로 묶는다. 권차가 있는 책은 낱권 ISBN이 따로 있어 권차도 서지에 둔다.
create sequence bib_record_seq increment by 50;

create table bib_record (
    id              bigint       not null,
    isbn13          varchar(13)  not null,
    title           varchar(500) not null,
    volume          varchar(20),
    volume_title    varchar(200),
    author          varchar(500),
    publisher       varchar(200),
    published_year  smallint,
    kdc             varchar(20),
    created_at      timestamptz  not null,
    updated_at      timestamptz  not null,
    constraint pk_bib_record primary key (id),
    constraint uk_bib_record_isbn13 unique (isbn13),
    constraint ck_bib_record_isbn13 check (isbn13 ~ '^97[89][0-9]{10}$')
);
