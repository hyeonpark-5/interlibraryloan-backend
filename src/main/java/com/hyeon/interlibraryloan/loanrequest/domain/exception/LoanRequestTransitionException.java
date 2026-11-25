package com.hyeon.interlibraryloan.loanrequest.domain.exception;

import lombok.Getter;

/**
 * 상태머신이 행동을 거부했다. 거부 이유를 함께 담아, 나중에 API 오류 코드로 바꾼다.
 */
@Getter
public class LoanRequestTransitionException extends RuntimeException {

	public enum Rejection {
		/** 지금 상태에서는 할 수 없는 행동 */
		INVALID_STATE,
		/** 이 요청과의 관계로는 할 수 없는 행동 */
		NOT_PERMITTED,
		/** 이 관계로는 고를 수 없는 취소 사유 */
		CANCEL_REASON_NOT_ALLOWED,
		/** 본인이나 가족이 아닌 사람에게 대출하려 함 */
		INVALID_RECEIVER
	}

	private final Rejection rejection;

	public LoanRequestTransitionException(Rejection rejection, String message) {
		super(message);
		this.rejection = rejection;
	}
}
