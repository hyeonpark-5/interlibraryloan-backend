package com.hyeon.interlibraryloan.loanrequest.domain;

/**
 * 취소 사유를 고를 수 있는 쪽.
 */
public enum CancelReasonActorScope {
	/** 신청 이용자 또는 요청관 사서 */
	BORROWER_SIDE,
	/** 제공관 사서 */
	LENDER,
	/** 스케줄러, 배치 */
	SYSTEM
}
