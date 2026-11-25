package com.hyeon.interlibraryloan.member.domain.type;

/**
 * 계정 역할. 값 목록은 app_user 테이블의 ck_app_user_role과 같아야 한다.
 * <p>
 * 요청관과 제공관은 역할이 아니라 요청과의 관계라서 여기에 없다.
 */
public enum UserRole {
	/** 이용자 */
	PATRON,
	/** 사서 */
	LIBRARIAN,
	/** 도서관 관리자. 사서 업무에 더해 자관 정책과 사서 계정을 관리한다 */
	LIBRARY_MANAGER,
	/** 시스템 관리자. 상태 전환은 할 수 없다 */
	SYSTEM_ADMIN;

	/** 직원 정보(staff)를 갖는 역할인가 */
	public boolean isStaff() {
		return this != PATRON;
	}

	/** 소속 도서관이 있어야 하는 역할인가 */
	public boolean requiresLibrary() {
		return this == LIBRARIAN || this == LIBRARY_MANAGER;
	}
}
