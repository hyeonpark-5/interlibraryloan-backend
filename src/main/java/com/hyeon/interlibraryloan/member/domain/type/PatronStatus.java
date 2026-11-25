package com.hyeon.interlibraryloan.member.domain.type;

/**
 * 이용자 상태. 값 목록은 patron 테이블의 ck_patron_status와 같아야 한다.
 */
public enum PatronStatus {
	/** 정상 */
	ACTIVE,
	/** 대출 정지. 종료일은 suspendedUntil */
	SUSPENDED,
	/** 탈퇴 */
	WITHDRAWN
}
