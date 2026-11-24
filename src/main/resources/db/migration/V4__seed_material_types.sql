-- 참고자료, 귀중본, 연속간행물은 상호대차 대상에서 제외한다 (BR-01)
insert into material_type (code, name, interlibrary_allowed) values
    ('GENERAL',    '일반',       true),
    ('CHILDREN',   '어린이',     true),
    ('REFERENCE',  '참고자료',   false),
    ('RARE',       '귀중본',     false),
    ('PERIODICAL', '연속간행물', false);
