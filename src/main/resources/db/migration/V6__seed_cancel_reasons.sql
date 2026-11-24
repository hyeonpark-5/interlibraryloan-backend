-- 취소 사유. 도서관 간 메모는 이용자에게 보내지 않고, 이용자에게는 patron_message만 보낸다 (BR-06)
insert into cancel_reason (code, name, patron_message, actor_scope, memo_required) values
    ('PATRON_CANCEL',          '이용자 취소',        '신청이 취소되었습니다',                                             'BORROWER_SIDE', false),
    ('NOT_ON_SHELF',           '서가에 없음',        '제공 도서관에서 자료를 찾을 수 없어 신청이 취소되었습니다',         'LENDER',        false),
    ('DAMAGED',                '훼손',               '자료 상태 문제로 제공이 어려워 신청이 취소되었습니다',              'LENDER',        false),
    ('NON_CIRCULATING',        '상호대차 불가 자료', '상호대차가 제한된 자료로 확인되어 신청이 취소되었습니다',           'LENDER',        false),
    ('SHIP_DEADLINE_EXCEEDED', '발송 기한 초과',     '제공 도서관의 처리 기한이 지나 신청이 취소되었습니다',              'SYSTEM',        false),
    ('OTHER',                  '기타',               '제공 도서관 사정으로 신청이 취소되었습니다',                        'LENDER',        true);
