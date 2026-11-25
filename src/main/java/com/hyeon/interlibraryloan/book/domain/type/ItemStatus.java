package com.hyeon.interlibraryloan.book.domain.type;

/**
 * 소장 자료 상태. 값 목록은 item 테이블의 ck_item_status와 같아야 한다.
 * 상태 전환 규칙은 상호대차 상태머신에서 다룬다.
 */
public enum ItemStatus {
	/** 대출 가능 */
	AVAILABLE,
	/** 자관 대출 중 (합성 데이터 전용) */
	ON_LOAN,
	/** 상호대차 요청에 배정됨, 발송 전 */
	RESERVED,
	/** 상호대차로 다른 도서관에 나가 있음 */
	LENT_OUT,
	/** 소재 불명. 서가에 없음 */
	MISSING,
	/** 훼손 */
	DAMAGED,
	/** 수리 중 */
	IN_REPAIR,
	/** 분실 */
	LOST,
	/** 제적 */
	WITHDRAWN
}
