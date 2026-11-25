package com.hyeon.interlibraryloan.loanrequest.domain.type;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyeon.interlibraryloan.book.domain.type.ItemEvent;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CancelReasonCodeTest {

	@ParameterizedTest(name = "{0}: {1}, 자료 {2}")
	@CsvSource({
			"PATRON_CANCEL,          BORROWER_SIDE, RELEASE_TO_SHELF",
			"NOT_ON_SHELF,           LENDER,        RELEASE_MISSING",
			"DAMAGED,                LENDER,        RELEASE_DAMAGED",
			"NON_CIRCULATING,        LENDER,        RELEASE_NON_CIRCULATING",
			"SHIP_DEADLINE_EXCEEDED, SYSTEM,        RELEASE_TO_SHELF",
			"OTHER,                  LENDER,        RELEASE_TO_SHELF",
	})
	@DisplayName("BR-05 취소 사유마다 고를 수 있는 쪽과 자료 후속 처리가 정해져 있다")
	void scopeAndItemEvent(CancelReasonCode code, CancelReasonActorScope scope, ItemEvent itemEvent) {
		assertThat(code.getScope()).isEqualTo(scope);
		assertThat(code.getItemEvent()).isEqualTo(itemEvent);
	}

	@ParameterizedTest(name = "{0}")
	@CsvSource({
			"BORROWER_SIDE, REQUESTER;BORROWING_LIBRARIAN",
			"LENDER,        LENDING_LIBRARIAN",
			"SYSTEM,        SYSTEM",
	})
	@DisplayName("취소 사유 범위마다 허용하는 관계가 정해져 있다")
	void scopePermits(CancelReasonActorScope scope, String permitted) {
		ActorRelation[] expected = Arrays.stream(permitted.split(";"))
				.map(ActorRelation::valueOf)
				.toArray(ActorRelation[]::new);

		assertThat(Arrays.stream(ActorRelation.values()).filter(scope::permits))
				.containsExactlyInAnyOrder(expected);
	}
}
