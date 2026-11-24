package com.hyeon.interlibraryloan.book.domain;

import static com.hyeon.interlibraryloan.book.domain.InterlibraryIneligibleReason.MATERIAL_TYPE_NOT_ALLOWED;
import static com.hyeon.interlibraryloan.book.domain.InterlibraryIneligibleReason.NEW_BOOK_RESTRICTED;
import static com.hyeon.interlibraryloan.book.domain.InterlibraryIneligibleReason.NOT_AVAILABLE;
import static com.hyeon.interlibraryloan.book.domain.InterlibraryIneligibleReason.NOT_LENDABLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class InterlibraryEligibilityTest {

	private static final LocalDate TODAY = LocalDate.of(2026, 11, 25);
	private static final LocalDate LONG_AGO = LocalDate.of(2020, 1, 1);
	private static final int BLOCK_30_DAYS = 30;

	@Test
	@DisplayName("BR-01 모든 조건을 만족하면 걸리는 이유가 없다")
	void eligible() {
		assertThat(InterlibraryEligibility.evaluate(
				ItemStatus.AVAILABLE, true, true, LONG_AGO, BLOCK_30_DAYS, TODAY))
				.isEmpty();
	}

	@ParameterizedTest(name = "{0}")
	@EnumSource(value = ItemStatus.class, names = "AVAILABLE", mode = EnumSource.Mode.EXCLUDE)
	@DisplayName("BR-01 대출 가능 상태가 아니면 배정할 수 없다")
	void notAvailable(ItemStatus status) {
		assertThat(InterlibraryEligibility.evaluate(
				status, true, true, LONG_AGO, BLOCK_30_DAYS, TODAY))
				.containsExactly(NOT_AVAILABLE);
	}

	@Test
	@DisplayName("BR-01 상호대차 허용이 꺼진 자료는 배정할 수 없다")
	void notLendable() {
		assertThat(InterlibraryEligibility.evaluate(
				ItemStatus.AVAILABLE, false, true, LONG_AGO, BLOCK_30_DAYS, TODAY))
				.containsExactly(NOT_LENDABLE);
	}

	@Test
	@DisplayName("BR-01 상호대차 불가 자료 유형은 배정할 수 없다")
	void materialTypeNotAllowed() {
		assertThat(InterlibraryEligibility.evaluate(
				ItemStatus.AVAILABLE, true, false, LONG_AGO, BLOCK_30_DAYS, TODAY))
				.containsExactly(MATERIAL_TYPE_NOT_ALLOWED);
	}

	@Test
	@DisplayName("BR-01 입수 후 제한 기간이 하루라도 남았으면 신간 제한에 걸린다")
	void newBookRestrictedUntilLastDay() {
		// given: 30일 제한, 입수 29일째
		LocalDate acquiredAt = TODAY.minusDays(29);

		// when, then
		assertThat(InterlibraryEligibility.evaluate(
				ItemStatus.AVAILABLE, true, true, acquiredAt, BLOCK_30_DAYS, TODAY))
				.containsExactly(NEW_BOOK_RESTRICTED);
	}

	@Test
	@DisplayName("BR-01 입수일에서 제한 일수가 지난 날부터는 배정할 수 있다")
	void newBookAllowedFromBoundaryDay() {
		// given: 30일 제한, 입수 30일째
		LocalDate acquiredAt = TODAY.minusDays(30);

		// when, then
		assertThat(InterlibraryEligibility.evaluate(
				ItemStatus.AVAILABLE, true, true, acquiredAt, BLOCK_30_DAYS, TODAY))
				.isEmpty();
	}

	@Test
	@DisplayName("신간 제한 일수가 0이면 오늘 입수한 자료도 배정할 수 있다")
	void noNewBookRestrictionWhenZero() {
		assertThat(InterlibraryEligibility.evaluate(
				ItemStatus.AVAILABLE, true, true, TODAY, 0, TODAY))
				.isEmpty();
	}

	@Test
	@DisplayName("입수일을 모르면 신간 제한을 적용하지 않는다")
	void noNewBookRestrictionWhenAcquiredAtUnknown() {
		assertThat(InterlibraryEligibility.evaluate(
				ItemStatus.AVAILABLE, true, true, null, BLOCK_30_DAYS, TODAY))
				.isEmpty();
	}

	@Test
	@DisplayName("여러 조건에 걸리면 이유를 모두 돌려준다")
	void collectsAllReasons() {
		assertThat(InterlibraryEligibility.evaluate(
				ItemStatus.DAMAGED, false, false, TODAY, BLOCK_30_DAYS, TODAY))
				.containsExactlyInAnyOrder(NOT_AVAILABLE, NOT_LENDABLE, MATERIAL_TYPE_NOT_ALLOWED, NEW_BOOK_RESTRICTED);
	}

	@Test
	@DisplayName("신간 제한 일수가 음수면 예외가 난다")
	void rejectsNegativeBlockDays() {
		assertThatThrownBy(() -> InterlibraryEligibility.evaluate(
				ItemStatus.AVAILABLE, true, true, LONG_AGO, -1, TODAY))
				.isInstanceOf(IllegalArgumentException.class);
	}
}
