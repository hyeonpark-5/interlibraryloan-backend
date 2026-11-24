package com.hyeon.interlibraryloan.book.domain;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * 소장 자료를 상호대차로 내줄 수 있는지 판단한다 (BR-01 사전 차단 중 자료 부분).
 * <p>
 * 엔티티와 Spring에 의존하지 않는 순수 규칙이라, 경계값까지 단위 테스트로 촘촘히 확인한다.
 * 걸리는 이유를 모두 돌려준다. 비어 있으면 배정 가능하다.
 */
public final class InterlibraryEligibility {

	private InterlibraryEligibility() {
	}

	/**
	 * @param newBookBlockDays 입수 후 이 기간 안의 자료는 불가. 0이면 제한 없음
	 * @param acquiredAt       입수일. 모르면 null이고, 이때 신간 제한은 적용하지 않는다
	 */
	public static Set<InterlibraryIneligibleReason> evaluate(
			ItemStatus status,
			boolean itemLendable,
			boolean materialTypeAllowed,
			LocalDate acquiredAt,
			int newBookBlockDays,
			LocalDate today) {
		Objects.requireNonNull(status, "status");
		Objects.requireNonNull(today, "today");
		if (newBookBlockDays < 0) {
			throw new IllegalArgumentException("newBookBlockDays는 0 이상이어야 합니다");
		}

		Set<InterlibraryIneligibleReason> reasons = EnumSet.noneOf(InterlibraryIneligibleReason.class);
		if (status != ItemStatus.AVAILABLE) {
			reasons.add(InterlibraryIneligibleReason.NOT_AVAILABLE);
		}
		if (!itemLendable) {
			reasons.add(InterlibraryIneligibleReason.NOT_LENDABLE);
		}
		if (!materialTypeAllowed) {
			reasons.add(InterlibraryIneligibleReason.MATERIAL_TYPE_NOT_ALLOWED);
		}
		if (isNewBookRestricted(acquiredAt, newBookBlockDays, today)) {
			reasons.add(InterlibraryIneligibleReason.NEW_BOOK_RESTRICTED);
		}
		return reasons;
	}

	/** 입수일 + 제한 일수가 되는 날부터 허용한다. */
	private static boolean isNewBookRestricted(LocalDate acquiredAt, int newBookBlockDays, LocalDate today) {
		if (acquiredAt == null || newBookBlockDays == 0) {
			return false;
		}
		return today.isBefore(acquiredAt.plusDays(newBookBlockDays));
	}
}
