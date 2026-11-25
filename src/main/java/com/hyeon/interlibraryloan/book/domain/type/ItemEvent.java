package com.hyeon.interlibraryloan.book.domain.type;

import com.hyeon.interlibraryloan.book.domain.policy.ItemStatusTransitions;

/**
 * 소장 자료 상태를 바꾸는 사건. 전환 규칙은 {@link ItemStatusTransitions}에 있다.
 */
public enum ItemEvent {
	/** 상호대차 신청으로 배정 */
	RESERVE,
	/** 발송 */
	LEND_OUT,
	/** 배정 해제, 서가로 돌아감 (이용자 취소, 발송 기한 초과, 기타 사유) */
	RELEASE_TO_SHELF,
	/** 배정 해제, 상호대차 불가 자료로 확인됨. 상태는 대출 가능이고 상호대차 허용을 끈다 */
	RELEASE_NON_CIRCULATING,
	/** 배정 해제, 서가에 없음 */
	RELEASE_MISSING,
	/** 배정 해제, 훼손 발견 */
	RELEASE_DAMAGED,
	/** 회송 검수 정상 */
	RETURN_NORMAL,
	/** 회송 검수 훼손 */
	RETURN_DAMAGED,
	/** 상호대차 중 분실 */
	LOSE,
	/** 소재 불명 자료 발견 */
	FIND,
	/** 수리 보냄 */
	SEND_TO_REPAIR,
	/** 수리 완료 */
	FINISH_REPAIR,
	/** 제적 */
	WITHDRAW
}
