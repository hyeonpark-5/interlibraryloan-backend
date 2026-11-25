package com.hyeon.interlibraryloan.loanrequest.domain.type;

/**
 * 상호대차 요청에 가할 수 있는 행동. 상태 이력의 action 값과 같다.
 */
public enum LoanRequestAction {
	/** 신청 */
	REQUEST,
	/** 발송 */
	SHIP,
	/** 취소 (이용자 취소, 발송 불가, 발송 기한 초과) */
	CANCEL,
	/** 도착 확인 */
	ARRIVE,
	/** 수령, 대출 처리 */
	CHECK_OUT,
	/** 수령 기한 초과 처리 */
	EXPIRE_PICKUP,
	/** 반납 */
	CHECK_IN,
	/** 분실 처리 */
	REPORT_LOST,
	/** 회송 */
	RETURN_SHIP,
	/** 회송 도착, 검수, 완료 */
	COMPLETE
}
