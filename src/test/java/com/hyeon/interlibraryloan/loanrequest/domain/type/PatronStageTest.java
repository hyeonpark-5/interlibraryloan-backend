package com.hyeon.interlibraryloan.loanrequest.domain.type;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class PatronStageTest {

	@ParameterizedTest(name = "{0} -> {1}")
	@CsvSource({
			"REQUESTED,      PREPARING_SHIPMENT",
			"SHIPPED,        IN_TRANSIT",
			"ARRIVED,        READY_FOR_PICKUP",
			"ON_LOAN,        ON_LOAN",
			"RETURNED,       RETURNED",
			"RETURN_SHIPPED, RETURNED",
			"COMPLETED,      RETURNED",
			"UNCLAIMED,      PICKUP_EXPIRED",
			"LOST,           LOST",
	})
	@DisplayName("이용자는 반납한 뒤의 회송과 검수를 보지 않고 반납 완료로 본다")
	void mapsStatusToStage(LoanRequestStatus status, PatronStage expected) {
		assertThat(PatronStage.of(status, null)).isEqualTo(expected);
	}

	@Test
	@DisplayName("본인이 취소하면 취소됨으로 보인다")
	void patronCancel() {
		assertThat(PatronStage.of(LoanRequestStatus.CANCELLED, CancelReasonCode.PATRON_CANCEL))
				.isEqualTo(PatronStage.CANCELLED);
	}

	@ParameterizedTest(name = "{0}")
	@EnumSource(value = CancelReasonCode.class, names = "PATRON_CANCEL", mode = EnumSource.Mode.EXCLUDE)
	@DisplayName("그 외 사유로 취소되면 제공 불가로 보인다")
	void otherCancelIsNotSupplied(CancelReasonCode reason) {
		assertThat(PatronStage.of(LoanRequestStatus.CANCELLED, reason)).isEqualTo(PatronStage.NOT_SUPPLIED);
	}

	@Test
	@DisplayName("취소 상태인데 사유가 없으면 예외가 난다")
	void cancelledRequiresReason() {
		assertThatThrownBy(() -> PatronStage.of(LoanRequestStatus.CANCELLED, null))
				.isInstanceOf(NullPointerException.class);
	}
}
