package com.hyeon.interlibraryloan.book.domain.policy;

import static com.hyeon.interlibraryloan.book.domain.type.ItemStatus.AVAILABLE;
import static com.hyeon.interlibraryloan.book.domain.type.ItemStatus.DAMAGED;
import static com.hyeon.interlibraryloan.book.domain.type.ItemStatus.IN_REPAIR;
import static com.hyeon.interlibraryloan.book.domain.type.ItemStatus.LENT_OUT;
import static com.hyeon.interlibraryloan.book.domain.type.ItemStatus.LOST;
import static com.hyeon.interlibraryloan.book.domain.type.ItemStatus.MISSING;
import static com.hyeon.interlibraryloan.book.domain.type.ItemStatus.RESERVED;
import static com.hyeon.interlibraryloan.book.domain.type.ItemStatus.WITHDRAWN;

import com.hyeon.interlibraryloan.book.domain.exception.ItemTransitionException;
import com.hyeon.interlibraryloan.book.domain.type.ItemEvent;
import com.hyeon.interlibraryloan.book.domain.type.ItemStatus;
import java.util.Map;
import java.util.Objects;

/**
 * 소장 자료 상태 전환표. 표에 없는 전환은 모두 거부한다.
 * <p>
 * 자관 대출(AVAILABLE과 ON_LOAN 사이)은 기능이 없어 표에 넣지 않는다. 합성 데이터로만 그 상태를 만든다.
 */
public final class ItemStatusTransitions {

	private record Key(ItemStatus from, ItemEvent event) {
	}

	private static final Map<Key, ItemStatus> RULES = Map.ofEntries(
			rule(AVAILABLE, ItemEvent.RESERVE, RESERVED),
			rule(RESERVED, ItemEvent.LEND_OUT, LENT_OUT),
			rule(RESERVED, ItemEvent.RELEASE_TO_SHELF, AVAILABLE),
			rule(RESERVED, ItemEvent.RELEASE_NON_CIRCULATING, AVAILABLE),
			rule(RESERVED, ItemEvent.RELEASE_MISSING, MISSING),
			rule(RESERVED, ItemEvent.RELEASE_DAMAGED, DAMAGED),
			rule(LENT_OUT, ItemEvent.RETURN_NORMAL, AVAILABLE),
			rule(LENT_OUT, ItemEvent.RETURN_DAMAGED, DAMAGED),
			rule(LENT_OUT, ItemEvent.LOSE, LOST),
			rule(MISSING, ItemEvent.FIND, AVAILABLE),
			rule(DAMAGED, ItemEvent.SEND_TO_REPAIR, IN_REPAIR),
			rule(IN_REPAIR, ItemEvent.FINISH_REPAIR, AVAILABLE),
			rule(DAMAGED, ItemEvent.WITHDRAW, WITHDRAWN),
			rule(MISSING, ItemEvent.WITHDRAW, WITHDRAWN),
			rule(LOST, ItemEvent.WITHDRAW, WITHDRAWN));

	private ItemStatusTransitions() {
	}

	public static ItemStatus next(ItemStatus from, ItemEvent event) {
		Objects.requireNonNull(from, "from");
		Objects.requireNonNull(event, "event");
		ItemStatus to = RULES.get(new Key(from, event));
		if (to == null) {
			throw new ItemTransitionException(from, event);
		}
		return to;
	}

	private static Map.Entry<Key, ItemStatus> rule(ItemStatus from, ItemEvent event, ItemStatus to) {
		return Map.entry(new Key(from, event), to);
	}
}
