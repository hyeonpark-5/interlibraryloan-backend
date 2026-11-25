package com.hyeon.interlibraryloan.loanrequest.domain.type;

import com.hyeon.interlibraryloan.book.domain.type.ItemEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 취소 사유 코드와 그 동작.
 * <p>
 * 누가 고를 수 있는지와 자료 후속 처리는 코드가 정한다.
 * 화면 표시명과 이용자 안내 문구는 cancel_reason 테이블에 있다.
 * 두 곳의 코드 목록과 고를 수 있는 쪽은 같아야 하며, 테스트로 확인한다.
 */
@Getter
@RequiredArgsConstructor
public enum CancelReasonCode {

	PATRON_CANCEL(CancelReasonActorScope.BORROWER_SIDE, ItemEvent.RELEASE_TO_SHELF),
	NOT_ON_SHELF(CancelReasonActorScope.LENDER, ItemEvent.RELEASE_MISSING),
	DAMAGED(CancelReasonActorScope.LENDER, ItemEvent.RELEASE_DAMAGED),
	NON_CIRCULATING(CancelReasonActorScope.LENDER, ItemEvent.RELEASE_NON_CIRCULATING),
	SHIP_DEADLINE_EXCEEDED(CancelReasonActorScope.SYSTEM, ItemEvent.RELEASE_TO_SHELF),
	OTHER(CancelReasonActorScope.LENDER, ItemEvent.RELEASE_TO_SHELF);

	/** 이 사유를 고를 수 있는 쪽 */
	private final CancelReasonActorScope scope;

	/** 취소 시 배정 자료에 일어나는 일 */
	private final ItemEvent itemEvent;
}
