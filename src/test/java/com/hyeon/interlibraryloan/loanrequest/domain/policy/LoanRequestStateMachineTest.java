package com.hyeon.interlibraryloan.loanrequest.domain.policy;

import static com.hyeon.interlibraryloan.loanrequest.domain.type.ActorRelation.BORROWING_LIBRARIAN;
import static com.hyeon.interlibraryloan.loanrequest.domain.type.ActorRelation.FAMILY;
import static com.hyeon.interlibraryloan.loanrequest.domain.type.ActorRelation.LENDING_LIBRARIAN;
import static com.hyeon.interlibraryloan.loanrequest.domain.type.ActorRelation.NONE;
import static com.hyeon.interlibraryloan.loanrequest.domain.type.ActorRelation.REQUESTER;
import static com.hyeon.interlibraryloan.loanrequest.domain.type.ActorRelation.SYSTEM;
import static com.hyeon.interlibraryloan.loanrequest.domain.type.LoanRequestAction.ARRIVE;
import static com.hyeon.interlibraryloan.loanrequest.domain.type.LoanRequestAction.CANCEL;
import static com.hyeon.interlibraryloan.loanrequest.domain.type.LoanRequestAction.CHECK_IN;
import static com.hyeon.interlibraryloan.loanrequest.domain.type.LoanRequestAction.CHECK_OUT;
import static com.hyeon.interlibraryloan.loanrequest.domain.type.LoanRequestAction.COMPLETE;
import static com.hyeon.interlibraryloan.loanrequest.domain.type.LoanRequestAction.EXPIRE_PICKUP;
import static com.hyeon.interlibraryloan.loanrequest.domain.type.LoanRequestAction.REPORT_LOST;
import static com.hyeon.interlibraryloan.loanrequest.domain.type.LoanRequestAction.REQUEST;
import static com.hyeon.interlibraryloan.loanrequest.domain.type.LoanRequestAction.RETURN_SHIP;
import static com.hyeon.interlibraryloan.loanrequest.domain.type.LoanRequestAction.SHIP;
import static com.hyeon.interlibraryloan.loanrequest.domain.type.LoanRequestStatus.ARRIVED;
import static com.hyeon.interlibraryloan.loanrequest.domain.type.LoanRequestStatus.ON_LOAN;
import static com.hyeon.interlibraryloan.loanrequest.domain.type.LoanRequestStatus.REQUESTED;
import static com.hyeon.interlibraryloan.loanrequest.domain.type.LoanRequestStatus.RETURNED;
import static com.hyeon.interlibraryloan.loanrequest.domain.type.LoanRequestStatus.RETURN_SHIPPED;
import static com.hyeon.interlibraryloan.loanrequest.domain.type.LoanRequestStatus.SHIPPED;
import static com.hyeon.interlibraryloan.loanrequest.domain.type.LoanRequestStatus.UNCLAIMED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hyeon.interlibraryloan.loanrequest.domain.exception.LoanRequestTransitionException.Rejection;
import com.hyeon.interlibraryloan.loanrequest.domain.exception.LoanRequestTransitionException;
import com.hyeon.interlibraryloan.loanrequest.domain.type.ActorRelation;
import com.hyeon.interlibraryloan.loanrequest.domain.type.CancelReasonCode;
import com.hyeon.interlibraryloan.loanrequest.domain.type.LoanRequestAction;
import com.hyeon.interlibraryloan.loanrequest.domain.type.LoanRequestStatus;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class LoanRequestStateMachineTest {

	/**
	 * 테스트가 기대하는 허용 전환. 프로덕션 전환표와 따로 적어 두어야 표가 틀렸을 때 잡힌다.
	 * 값은 그 전환을 할 수 있는 관계다.
	 */
	private static final Map<String, Set<ActorRelation>> EXPECTED_ALLOWED = Map.of(
			"REQUESTED:SHIP", EnumSet.of(LENDING_LIBRARIAN),
			"REQUESTED:CANCEL", EnumSet.of(REQUESTER, BORROWING_LIBRARIAN, LENDING_LIBRARIAN, SYSTEM),
			"SHIPPED:ARRIVE", EnumSet.of(BORROWING_LIBRARIAN),
			"ARRIVED:CHECK_OUT", EnumSet.of(BORROWING_LIBRARIAN),
			"ARRIVED:EXPIRE_PICKUP", EnumSet.of(SYSTEM, BORROWING_LIBRARIAN),
			"ON_LOAN:CHECK_IN", EnumSet.of(BORROWING_LIBRARIAN),
			"ON_LOAN:REPORT_LOST", EnumSet.of(BORROWING_LIBRARIAN),
			"RETURNED:RETURN_SHIP", EnumSet.of(BORROWING_LIBRARIAN),
			"UNCLAIMED:RETURN_SHIP", EnumSet.of(BORROWING_LIBRARIAN),
			"RETURN_SHIPPED:COMPLETE", EnumSet.of(LENDING_LIBRARIAN));

	/** 각 행동을 시도할 때 쓸, 그 행동이 허용된 관계 하나. 상태만 틀리게 만들기 위해 쓴다. */
	private static final Map<LoanRequestAction, ActorRelation> PERMITTED_RELATION = Map.of(
			SHIP, LENDING_LIBRARIAN,
			CANCEL, REQUESTER,
			ARRIVE, BORROWING_LIBRARIAN,
			CHECK_OUT, BORROWING_LIBRARIAN,
			EXPIRE_PICKUP, SYSTEM,
			CHECK_IN, BORROWING_LIBRARIAN,
			REPORT_LOST, BORROWING_LIBRARIAN,
			RETURN_SHIP, BORROWING_LIBRARIAN,
			COMPLETE, LENDING_LIBRARIAN);

	/** 테스트용으로 행동 종류에 맞는 메서드를 부른다. */
	private static LoanRequestStatus attempt(LoanRequestStatus from, LoanRequestAction action, ActorRelation relation) {
		return switch (action) {
			case CANCEL -> LoanRequestStateMachine.cancel(from, CancelReasonCode.PATRON_CANCEL, relation);
			case CHECK_OUT -> LoanRequestStateMachine.checkOut(from, relation, REQUESTER);
			default -> LoanRequestStateMachine.transit(from, action, relation);
		};
	}

	private static void assertRejected(ThrowingCallable call, Rejection expected) {
		assertThatThrownBy(call)
				.isInstanceOf(LoanRequestTransitionException.class)
				.extracting("rejection")
				.isEqualTo(expected);
	}

	@Nested
	@DisplayName("신청")
	class Request {

		@Test
		@DisplayName("신청자 본인이 신청하면 발송 대기 상태가 된다")
		void requesterCanRequest() {
			assertThat(LoanRequestStateMachine.request(REQUESTER)).isEqualTo(REQUESTED);
		}

		@ParameterizedTest(name = "{0}")
		@EnumSource(value = ActorRelation.class, names = "REQUESTER", mode = EnumSource.Mode.EXCLUDE)
		@DisplayName("신청자 본인이 아니면 신청할 수 없다 (사서 대리 신청은 1차 범위 밖)")
		void othersCannotRequest(ActorRelation relation) {
			assertRejected(() -> LoanRequestStateMachine.request(relation), Rejection.NOT_PERMITTED);
		}
	}

	@Nested
	@DisplayName("허용 전환")
	class Allowed {

		@ParameterizedTest(name = "{0} --{1} by {2}--> {3}")
		@CsvSource({
				"REQUESTED,      SHIP,          LENDING_LIBRARIAN,   SHIPPED",
				"SHIPPED,        ARRIVE,        BORROWING_LIBRARIAN, ARRIVED",
				"ARRIVED,        EXPIRE_PICKUP, SYSTEM,              UNCLAIMED",
				"ARRIVED,        EXPIRE_PICKUP, BORROWING_LIBRARIAN, UNCLAIMED",
				"ON_LOAN,        CHECK_IN,      BORROWING_LIBRARIAN, RETURNED",
				"ON_LOAN,        REPORT_LOST,   BORROWING_LIBRARIAN, LOST",
				"RETURNED,       RETURN_SHIP,   BORROWING_LIBRARIAN, RETURN_SHIPPED",
				"UNCLAIMED,      RETURN_SHIP,   BORROWING_LIBRARIAN, RETURN_SHIPPED",
				"RETURN_SHIPPED, COMPLETE,      LENDING_LIBRARIAN,   COMPLETED",
		})
		@DisplayName("전환표의 행동은 기대한 다음 상태로 간다")
		void simpleTransitions(LoanRequestStatus from, LoanRequestAction action, ActorRelation relation,
				LoanRequestStatus expected) {
			assertThat(LoanRequestStateMachine.transit(from, action, relation)).isEqualTo(expected);
		}

		@ParameterizedTest(name = "{0} by {1}")
		@CsvSource({
				"PATRON_CANCEL,          REQUESTER",
				"PATRON_CANCEL,          BORROWING_LIBRARIAN",
				"NOT_ON_SHELF,           LENDING_LIBRARIAN",
				"DAMAGED,                LENDING_LIBRARIAN",
				"NON_CIRCULATING,        LENDING_LIBRARIAN",
				"OTHER,                  LENDING_LIBRARIAN",
				"SHIP_DEADLINE_EXCEEDED, SYSTEM",
		})
		@DisplayName("BR-08 발송 전에는 관계에 맞는 사유로 취소할 수 있다")
		void cancel(CancelReasonCode reason, ActorRelation relation) {
			assertThat(LoanRequestStateMachine.cancel(REQUESTED, reason, relation))
					.isEqualTo(LoanRequestStatus.CANCELLED);
		}

		@ParameterizedTest(name = "받는 사람 {0}")
		@EnumSource(value = ActorRelation.class, names = {"REQUESTER", "FAMILY"})
		@DisplayName("BR-16 요청관 사서는 신청자 본인이나 가족에게 대출할 수 있다")
		void checkOut(ActorRelation receiver) {
			assertThat(LoanRequestStateMachine.checkOut(ARRIVED, BORROWING_LIBRARIAN, receiver))
					.isEqualTo(ON_LOAN);
		}
	}

	@Nested
	@DisplayName("상태 때문에 거부")
	class InvalidState {

		static Stream<Arguments> forbiddenPairs() {
			List<Arguments> pairs = new ArrayList<>();
			for (LoanRequestStatus status : LoanRequestStatus.values()) {
				for (LoanRequestAction action : LoanRequestAction.values()) {
					if (action != REQUEST && !EXPECTED_ALLOWED.containsKey(status + ":" + action)) {
						pairs.add(Arguments.of(status, action));
					}
				}
			}
			return pairs.stream();
		}

		@ParameterizedTest(name = "{0} 상태에서 {1}")
		@MethodSource("forbiddenPairs")
		@DisplayName("전환표에 없는 (상태, 행동) 조합은 허용된 관계가 시도해도 모두 거부된다")
		void allOtherPairsAreRejected(LoanRequestStatus from, LoanRequestAction action) {
			ActorRelation relation = PERMITTED_RELATION.get(action);

			assertRejected(() -> attempt(from, action, relation), Rejection.INVALID_STATE);
		}

		@Test
		@DisplayName("금지 조합 수는 상태 10개 x 행동 9개 - 허용 10개 = 80개다")
		void forbiddenPairCount() {
			assertThat(forbiddenPairs()).hasSize(80);
		}

		@Test
		@DisplayName("상태와 관계가 모두 틀리면 상태 문제를 먼저 알린다")
		void stateIsCheckedBeforeRelation() {
			assertRejected(() -> LoanRequestStateMachine.transit(ON_LOAN, SHIP, REQUESTER), Rejection.INVALID_STATE);
		}
	}

	@Nested
	@DisplayName("관계 때문에 거부")
	class NotPermitted {

		static Stream<Arguments> wrongRelations() {
			List<Arguments> cases = new ArrayList<>();
			EXPECTED_ALLOWED.forEach((key, allowed) -> {
				String[] parts = key.split(":");
				LoanRequestStatus from = LoanRequestStatus.valueOf(parts[0]);
				LoanRequestAction action = LoanRequestAction.valueOf(parts[1]);
				for (ActorRelation relation : ActorRelation.values()) {
					if (!allowed.contains(relation)) {
						cases.add(Arguments.of(from, action, relation));
					}
				}
			});
			return cases.stream();
		}

		@ParameterizedTest(name = "{2}이(가) {0} 상태에서 {1}")
		@MethodSource("wrongRelations")
		@DisplayName("허용된 전환이라도 관계가 맞지 않으면 거부된다")
		void wrongRelationIsRejected(LoanRequestStatus from, LoanRequestAction action, ActorRelation relation) {
			assertRejected(() -> attempt(from, action, relation), Rejection.NOT_PERMITTED);
		}

		@Test
		@DisplayName("같은 사서라도 이 요청의 요청관 쪽이면 발송할 수 없다")
		void borrowingLibrarianCannotShip() {
			assertRejected(() -> LoanRequestStateMachine.transit(REQUESTED, SHIP, BORROWING_LIBRARIAN),
					Rejection.NOT_PERMITTED);
		}

		@Test
		@DisplayName("같은 사서라도 이 요청의 제공관 쪽이면 도착 확인을 할 수 없다")
		void lendingLibrarianCannotArrive() {
			assertRejected(() -> LoanRequestStateMachine.transit(SHIPPED, ARRIVE, LENDING_LIBRARIAN),
					Rejection.NOT_PERMITTED);
		}

		@Test
		@DisplayName("BR-15 가족은 대신 취소할 수 없다")
		void familyCannotCancel() {
			assertRejected(() -> LoanRequestStateMachine.cancel(REQUESTED, CancelReasonCode.PATRON_CANCEL, FAMILY),
					Rejection.NOT_PERMITTED);
		}

		@Test
		@DisplayName("BR-19 요청과 관계없는 사람(시스템 관리자 포함)은 아무 전환도 할 수 없다")
		void unrelatedActorCannotDoAnything() {
			for (LoanRequestStatus status : LoanRequestStatus.values()) {
				assertThat(LoanRequestStateMachine.availableActions(status, NONE)).isEmpty();
			}
		}
	}

	@Nested
	@DisplayName("취소 사유와 받는 사람 검사")
	class ExtraChecks {

		@ParameterizedTest(name = "{1}이(가) {0} 사유로 취소")
		@CsvSource({
				"NOT_ON_SHELF,           REQUESTER",
				"OTHER,                  BORROWING_LIBRARIAN",
				"PATRON_CANCEL,          LENDING_LIBRARIAN",
				"DAMAGED,                SYSTEM",
				"SHIP_DEADLINE_EXCEEDED, BORROWING_LIBRARIAN",
				"SHIP_DEADLINE_EXCEEDED, LENDING_LIBRARIAN",
				"PATRON_CANCEL,          SYSTEM",
		})
		@DisplayName("관계에 맞지 않는 취소 사유는 고를 수 없다")
		void cancelReasonMustMatchRelation(CancelReasonCode reason, ActorRelation relation) {
			assertRejected(() -> LoanRequestStateMachine.cancel(REQUESTED, reason, relation),
					Rejection.CANCEL_REASON_NOT_ALLOWED);
		}

		@ParameterizedTest(name = "받는 사람 {0}")
		@EnumSource(value = ActorRelation.class, names = {"REQUESTER", "FAMILY"}, mode = EnumSource.Mode.EXCLUDE)
		@DisplayName("BR-16 신청자 본인이나 가족이 아니면 대출받을 수 없다")
		void receiverMustBeRequesterOrFamily(ActorRelation receiver) {
			assertRejected(() -> LoanRequestStateMachine.checkOut(ARRIVED, BORROWING_LIBRARIAN, receiver),
					Rejection.INVALID_RECEIVER);
		}

		@Test
		@DisplayName("가족은 수령은 할 수 있어도 스스로 대출 처리를 할 수는 없다")
		void familyCannotCheckOutByThemselves() {
			assertRejected(() -> LoanRequestStateMachine.checkOut(ARRIVED, FAMILY, FAMILY), Rejection.NOT_PERMITTED);
		}

		@ParameterizedTest(name = "{0}")
		@EnumSource(value = LoanRequestAction.class, names = {"REQUEST", "CANCEL", "CHECK_OUT"})
		@DisplayName("추가 입력이 필요한 행동을 공통 메서드로 부르면 프로그래밍 오류로 막는다")
		void dedicatedActionsCannotUseTransit(LoanRequestAction action) {
			assertThatThrownBy(() -> LoanRequestStateMachine.transit(REQUESTED, action, REQUESTER))
					.isInstanceOf(IllegalArgumentException.class);
		}
	}

	@Nested
	@DisplayName("할 수 있는 행동 목록")
	class AvailableActions {

		@Test
		@DisplayName("발송 대기 중인 요청에서 제공관 사서는 발송과 발송 불가를, 신청자는 취소만 할 수 있다")
		void requested() {
			assertThat(LoanRequestStateMachine.availableActions(REQUESTED, LENDING_LIBRARIAN))
					.containsExactlyInAnyOrder(SHIP, CANCEL);
			assertThat(LoanRequestStateMachine.availableActions(REQUESTED, REQUESTER))
					.containsExactly(CANCEL);
			assertThat(LoanRequestStateMachine.availableActions(REQUESTED, BORROWING_LIBRARIAN))
					.containsExactly(CANCEL);
		}

		@Test
		@DisplayName("도착한 요청에서 요청관 사서는 대출과 미수령 처리를 할 수 있다")
		void arrived() {
			assertThat(LoanRequestStateMachine.availableActions(ARRIVED, BORROWING_LIBRARIAN))
					.containsExactlyInAnyOrder(CHECK_OUT, EXPIRE_PICKUP);
		}

		@Test
		@DisplayName("BR-10 대출 중인 이용자가 할 수 있는 행동은 없다 (연장 없음)")
		void onLoanRequesterHasNoActions() {
			assertThat(LoanRequestStateMachine.availableActions(ON_LOAN, REQUESTER)).isEmpty();
		}

		@ParameterizedTest(name = "{0}")
		@EnumSource(value = LoanRequestStatus.class, names = {"CANCELLED", "COMPLETED", "LOST"})
		@DisplayName("종료 상태에서는 누구도 아무 행동도 할 수 없다")
		void terminalStatesHaveNoActions(LoanRequestStatus terminal) {
			for (ActorRelation relation : ActorRelation.values()) {
				assertThat(LoanRequestStateMachine.availableActions(terminal, relation)).isEmpty();
			}
		}

		@Test
		@DisplayName("가족은 어떤 상태에서도 직접 하는 행동이 없다 (조회와 수령만)")
		void familyHasNoActions() {
			for (LoanRequestStatus status : LoanRequestStatus.values()) {
				assertThat(LoanRequestStateMachine.availableActions(status, FAMILY)).isEmpty();
			}
		}

		@Test
		@DisplayName("회송 대기와 미수령 요청 모두 요청관 사서가 회송할 수 있다")
		void returnShip() {
			assertThat(LoanRequestStateMachine.availableActions(RETURNED, BORROWING_LIBRARIAN))
					.containsExactly(RETURN_SHIP);
			assertThat(LoanRequestStateMachine.availableActions(UNCLAIMED, BORROWING_LIBRARIAN))
					.containsExactly(RETURN_SHIP);
			assertThat(LoanRequestStateMachine.availableActions(RETURN_SHIPPED, LENDING_LIBRARIAN))
					.containsExactly(COMPLETE);
		}
	}
}
