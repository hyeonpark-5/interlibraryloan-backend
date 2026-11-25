package com.hyeon.interlibraryloan.book.domain.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hyeon.interlibraryloan.book.domain.exception.ItemTransitionException;
import com.hyeon.interlibraryloan.book.domain.type.ItemEvent;
import com.hyeon.interlibraryloan.book.domain.type.ItemStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class ItemStatusTransitionsTest {

	/** 테스트가 기대하는 허용 전환. 프로덕션 표와 따로 적는다. */
	private static final Set<String> EXPECTED_ALLOWED = Set.of(
			"AVAILABLE:RESERVE",
			"RESERVED:LEND_OUT",
			"RESERVED:RELEASE_TO_SHELF",
			"RESERVED:RELEASE_NON_CIRCULATING",
			"RESERVED:RELEASE_MISSING",
			"RESERVED:RELEASE_DAMAGED",
			"LENT_OUT:RETURN_NORMAL",
			"LENT_OUT:RETURN_DAMAGED",
			"LENT_OUT:LOSE",
			"MISSING:FIND",
			"DAMAGED:SEND_TO_REPAIR",
			"IN_REPAIR:FINISH_REPAIR",
			"DAMAGED:WITHDRAW",
			"MISSING:WITHDRAW",
			"LOST:WITHDRAW");

	@ParameterizedTest(name = "{0} --{1}--> {2}")
	@CsvSource({
			"AVAILABLE, RESERVE,                 RESERVED",
			"RESERVED,  LEND_OUT,                LENT_OUT",
			"RESERVED,  RELEASE_TO_SHELF,        AVAILABLE",
			"RESERVED,  RELEASE_NON_CIRCULATING, AVAILABLE",
			"RESERVED,  RELEASE_MISSING,         MISSING",
			"RESERVED,  RELEASE_DAMAGED,         DAMAGED",
			"LENT_OUT,  RETURN_NORMAL,           AVAILABLE",
			"LENT_OUT,  RETURN_DAMAGED,          DAMAGED",
			"LENT_OUT,  LOSE,                    LOST",
			"MISSING,   FIND,                    AVAILABLE",
			"DAMAGED,   SEND_TO_REPAIR,          IN_REPAIR",
			"IN_REPAIR, FINISH_REPAIR,           AVAILABLE",
			"DAMAGED,   WITHDRAW,                WITHDRAWN",
			"MISSING,   WITHDRAW,                WITHDRAWN",
			"LOST,      WITHDRAW,                WITHDRAWN",
	})
	@DisplayName("자료 상태 전환표의 사건은 기대한 다음 상태로 간다")
	void allowed(ItemStatus from, ItemEvent event, ItemStatus expected) {
		assertThat(ItemStatusTransitions.next(from, event)).isEqualTo(expected);
	}

	static Stream<Arguments> forbiddenPairs() {
		List<Arguments> pairs = new ArrayList<>();
		for (ItemStatus status : ItemStatus.values()) {
			for (ItemEvent event : ItemEvent.values()) {
				if (!EXPECTED_ALLOWED.contains(status + ":" + event)) {
					pairs.add(Arguments.of(status, event));
				}
			}
		}
		return pairs.stream();
	}

	@ParameterizedTest(name = "{0} 상태에 {1}")
	@MethodSource("forbiddenPairs")
	@DisplayName("전환표에 없는 (상태, 사건) 조합은 모두 거부된다")
	void forbidden(ItemStatus from, ItemEvent event) {
		assertThatThrownBy(() -> ItemStatusTransitions.next(from, event))
				.isInstanceOf(ItemTransitionException.class);
	}

	@Test
	@DisplayName("금지 조합 수는 상태 9개 x 사건 13개 - 허용 15개 = 102개다")
	void forbiddenPairCount() {
		assertThat(forbiddenPairs()).hasSize(102);
	}

	@Test
	@DisplayName("제적된 자료는 어떤 사건으로도 상태가 바뀌지 않는다")
	void withdrawnIsFinal() {
		for (ItemEvent event : ItemEvent.values()) {
			assertThatThrownBy(() -> ItemStatusTransitions.next(ItemStatus.WITHDRAWN, event))
					.isInstanceOf(ItemTransitionException.class);
		}
	}
}
