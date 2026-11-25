package com.hyeon.interlibraryloan.loanrequest.domain.type;

/**
 * 행동하는 사람이 이 요청에서 어느 쪽인가.
 * <p>
 * 요청관과 제공관은 역할이 아니라 요청과의 관계다. 같은 사서도 요청마다 관계가 다르다.
 * 관계 계산은 DB 조회가 필요하므로 애플리케이션 계층이 하고, 상태머신은 계산된 관계만 받는다.
 */
public enum ActorRelation {
	/** 신청한 이용자 본인 */
	REQUESTER,
	/** 신청자와 같은 가족 그룹의 활성 구성원 */
	FAMILY,
	/** 소속 도서관이 이 요청의 요청관인 사서, 도서관 관리자 */
	BORROWING_LIBRARIAN,
	/** 소속 도서관이 이 요청의 제공관인 사서, 도서관 관리자 */
	LENDING_LIBRARIAN,
	/** 스케줄러, 배치, 컨슈머 */
	SYSTEM,
	/** 이 요청과 관계없는 사람. 시스템 관리자도 여기에 해당한다(BR-19) */
	NONE
}
