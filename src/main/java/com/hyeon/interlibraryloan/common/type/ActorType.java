package com.hyeon.interlibraryloan.common.type;

/**
 * 행동한 사람의 유형. 상태 이력의 actor_type과 같다.
 * 도서관 관리자도 상태 전환을 할 때는 사서(LIBRARIAN)로 기록한다.
 */
public enum ActorType {
	PATRON,
	LIBRARIAN,
	SYSTEM
}
