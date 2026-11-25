package com.hyeon.interlibraryloan.loanrequest.domain.type;

/**
 * 상호대차 요청 상태.
 * <p>
 * 값 목록과 "진행 중" 두 기준은 loan_request 테이블의 CHECK 제약, 부분 유니크 인덱스와 같아야 한다.
 * 상태를 추가하면 switch 식이 컴파일 오류를 내서 기준을 정하지 않고 넘어갈 수 없다.
 */
public enum LoanRequestStatus {
	/** 신청됨, 자료 배정됨, 발송 대기 */
	REQUESTED,
	/** 제공관이 발송함 */
	SHIPPED,
	/** 요청관에 도착함, 수령 대기 */
	ARRIVED,
	/** 이용자가 수령함, 대출 중 */
	ON_LOAN,
	/** 수령 기한이 지남 */
	UNCLAIMED,
	/** 이용자가 반납함 */
	RETURNED,
	/** 요청관이 제공관으로 회송함 */
	RETURN_SHIPPED,
	/** 제공관이 검수하고 끝냄 */
	COMPLETED,
	/** 취소됨. 사유 코드로 이용자 취소와 발송 불가를 구분한다 */
	CANCELLED,
	/** 분실 */
	LOST;

	/** 종료 상태에서는 어떤 행동도 할 수 없다. */
	public boolean isTerminal() {
		return switch (this) {
			case CANCELLED, COMPLETED, LOST -> true;
			case REQUESTED, SHIPPED, ARRIVED, ON_LOAN, UNCLAIMED, RETURNED, RETURN_SHIPPED -> false;
		};
	}

	/**
	 * 이용자 기준 진행 중. 같은 서지 중복 신청 금지(BR-20)와 동시 진행 한도에 쓴다.
	 * 이용자는 반납한 순간 끝난 것이므로 반납 이후 상태는 포함하지 않는다.
	 */
	public boolean isPatronActive() {
		return switch (this) {
			case REQUESTED, SHIPPED, ARRIVED, ON_LOAN -> true;
			case UNCLAIMED, RETURNED, RETURN_SHIPPED, COMPLETED, CANCELLED, LOST -> false;
		};
	}

	/**
	 * 자료 기준 진행 중. 자료 하나에는 이 기준의 요청이 하나뿐이다(BR-04).
	 * 자료는 회송이 끝날 때까지 묶여 있으므로 종료 상태가 아닌 모든 상태다.
	 */
	public boolean isItemActive() {
		return !isTerminal();
	}
}
