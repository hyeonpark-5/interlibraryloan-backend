package com.hyeon.interlibraryloan.book.domain.exception;

import com.hyeon.interlibraryloan.book.domain.type.InterlibraryIneligibleReason;
import java.util.Set;
import lombok.Getter;

/**
 * 상호대차로 내줄 수 없는 자료를 배정하려 했다 (BR-01).
 */
@Getter
public class ItemNotEligibleException extends IllegalStateException {

	private final Set<InterlibraryIneligibleReason> reasons;

	public ItemNotEligibleException(Long itemId, Set<InterlibraryIneligibleReason> reasons) {
		super("자료 " + itemId + "은(는) 상호대차로 배정할 수 없습니다: " + reasons);
		this.reasons = Set.copyOf(reasons);
	}
}
