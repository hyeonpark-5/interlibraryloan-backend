package com.hyeon.interlibraryloan.loanrequest.domain.type;

import static com.hyeon.interlibraryloan.loanrequest.domain.type.LoanRequestStatus.ARRIVED;
import static com.hyeon.interlibraryloan.loanrequest.domain.type.LoanRequestStatus.CANCELLED;
import static com.hyeon.interlibraryloan.loanrequest.domain.type.LoanRequestStatus.COMPLETED;
import static com.hyeon.interlibraryloan.loanrequest.domain.type.LoanRequestStatus.LOST;
import static com.hyeon.interlibraryloan.loanrequest.domain.type.LoanRequestStatus.ON_LOAN;
import static com.hyeon.interlibraryloan.loanrequest.domain.type.LoanRequestStatus.REQUESTED;
import static com.hyeon.interlibraryloan.loanrequest.domain.type.LoanRequestStatus.RETURNED;
import static com.hyeon.interlibraryloan.loanrequest.domain.type.LoanRequestStatus.RETURN_SHIPPED;
import static com.hyeon.interlibraryloan.loanrequest.domain.type.LoanRequestStatus.SHIPPED;
import static com.hyeon.interlibraryloan.loanrequest.domain.type.LoanRequestStatus.UNCLAIMED;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.function.Predicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LoanRequestStatusTest {

	private static LoanRequestStatus[] matching(Predicate<LoanRequestStatus> predicate) {
		return Arrays.stream(LoanRequestStatus.values()).filter(predicate).toArray(LoanRequestStatus[]::new);
	}

	@Test
	@DisplayName("종료 상태는 취소, 완료, 분실이다")
	void terminal() {
		assertThat(matching(LoanRequestStatus::isTerminal))
				.containsExactlyInAnyOrder(CANCELLED, COMPLETED, LOST);
	}

	@Test
	@DisplayName("BR-20 이용자 기준 진행 중은 신청부터 대출 중까지다")
	void patronActive() {
		assertThat(matching(LoanRequestStatus::isPatronActive))
				.containsExactlyInAnyOrder(REQUESTED, SHIPPED, ARRIVED, ON_LOAN);
	}

	@Test
	@DisplayName("BR-04 자료 기준 진행 중은 회송이 끝날 때까지다")
	void itemActive() {
		assertThat(matching(LoanRequestStatus::isItemActive))
				.containsExactlyInAnyOrder(REQUESTED, SHIPPED, ARRIVED, ON_LOAN, UNCLAIMED, RETURNED, RETURN_SHIPPED);
	}
}
