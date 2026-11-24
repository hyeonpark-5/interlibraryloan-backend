-- 모든 도서관에 같은 기본 정책을 넣는다.
-- 실제 운영 기준이 확인되면 새 마이그레이션으로 값을 바꾼다.
insert into library_policy (library_id, loan_days, max_active_requests, ship_deadline_days, pickup_deadline_days, new_book_block_days, created_at, updated_at)
select id, 14, 3, 3, 7, 0, now(), now()
from library;
