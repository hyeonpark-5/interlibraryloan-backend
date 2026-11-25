package com.hyeon.interlibraryloan.loanrequest.domain.type;

import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 이용자에게 보여주는 진행 단계. 이용자는 내부 상태를 보지 않는다.
 */
@Getter
@RequiredArgsConstructor
public enum PatronStage {

	PREPARING_SHIPMENT("발송 준비 중"),
	IN_TRANSIT("배송 중"),
	READY_FOR_PICKUP("수령 대기"),
	ON_LOAN("대출 중"),
	RETURNED("반납 완료"),
	CANCELLED("취소됨"),
	NOT_SUPPLIED("제공 불가"),
	PICKUP_EXPIRED("수령 기한 만료"),
	LOST("분실");

	private final String label;

	/**
	 * @param cancelReason 상태가 CANCELLED일 때만 필요하다. 이용자 취소와 제공 불가를 구분한다
	 */
	public static PatronStage of(LoanRequestStatus status, CancelReasonCode cancelReason) {
		Objects.requireNonNull(status, "status");
		return switch (status) {
			case REQUESTED -> PREPARING_SHIPMENT;
			case SHIPPED -> IN_TRANSIT;
			case ARRIVED -> READY_FOR_PICKUP;
			case ON_LOAN -> ON_LOAN;
			case RETURNED, RETURN_SHIPPED, COMPLETED -> RETURNED;
			case UNCLAIMED -> PICKUP_EXPIRED;
			case LOST -> LOST;
			case CANCELLED -> {
				Objects.requireNonNull(cancelReason, "취소된 요청은 취소 사유가 필요합니다");
				yield cancelReason == CancelReasonCode.PATRON_CANCEL ? CANCELLED : NOT_SUPPLIED;
			}
		};
	}
}
