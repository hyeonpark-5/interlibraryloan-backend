package com.hyeon.interlibraryloan.loanrequest.domain.policy;

import static com.hyeon.interlibraryloan.loanrequest.domain.type.ActorRelation.BORROWING_LIBRARIAN;
import static com.hyeon.interlibraryloan.loanrequest.domain.type.ActorRelation.FAMILY;
import static com.hyeon.interlibraryloan.loanrequest.domain.type.ActorRelation.LENDING_LIBRARIAN;
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
import static com.hyeon.interlibraryloan.loanrequest.domain.type.LoanRequestStatus.CANCELLED;
import static com.hyeon.interlibraryloan.loanrequest.domain.type.LoanRequestStatus.COMPLETED;
import static com.hyeon.interlibraryloan.loanrequest.domain.type.LoanRequestStatus.LOST;
import static com.hyeon.interlibraryloan.loanrequest.domain.type.LoanRequestStatus.ON_LOAN;
import static com.hyeon.interlibraryloan.loanrequest.domain.type.LoanRequestStatus.REQUESTED;
import static com.hyeon.interlibraryloan.loanrequest.domain.type.LoanRequestStatus.RETURNED;
import static com.hyeon.interlibraryloan.loanrequest.domain.type.LoanRequestStatus.RETURN_SHIPPED;
import static com.hyeon.interlibraryloan.loanrequest.domain.type.LoanRequestStatus.SHIPPED;
import static com.hyeon.interlibraryloan.loanrequest.domain.type.LoanRequestStatus.UNCLAIMED;

import com.hyeon.interlibraryloan.loanrequest.domain.exception.LoanRequestTransitionException.Rejection;
import com.hyeon.interlibraryloan.loanrequest.domain.exception.LoanRequestTransitionException;
import com.hyeon.interlibraryloan.loanrequest.domain.type.ActorRelation;
import com.hyeon.interlibraryloan.loanrequest.domain.type.CancelReasonCode;
import com.hyeon.interlibraryloan.loanrequest.domain.type.LoanRequestAction;
import com.hyeon.interlibraryloan.loanrequest.domain.type.LoanRequestStatus;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 상호대차 요청 상태머신. 지금 상태에서 이 행동이 가능한지, 이 관계의 사람이 해도 되는지 판단하고 다음 상태를 정한다.
 * <p>
 * 표에 없는 전환은 모두 거부한다. 원본 표는 .claude/skills/loan-request-state/references/transitions.md다.
 * Spring과 DB에 의존하지 않는다.
 */
public final class LoanRequestStateMachine {

	private record Key(LoanRequestStatus from, LoanRequestAction action) {
	}

	private record Rule(LoanRequestStatus to, Set<ActorRelation> allowed) {
	}

	/** 행동마다 추가 입력이 필요해서 전용 메서드로만 처리하는 행동 */
	private static final Set<LoanRequestAction> DEDICATED_ACTIONS = EnumSet.of(REQUEST, CANCEL, CHECK_OUT);

	/** 대출 받는 사람이 될 수 있는 관계 (BR-15, BR-16 가족 대리 수령) */
	private static final Set<ActorRelation> RECEIVERS = EnumSet.of(REQUESTER, FAMILY);

	private static final Map<Key, Rule> RULES = Map.ofEntries(
			rule(REQUESTED, SHIP, SHIPPED, LENDING_LIBRARIAN),
			rule(REQUESTED, CANCEL, CANCELLED, REQUESTER, BORROWING_LIBRARIAN, LENDING_LIBRARIAN, SYSTEM),
			rule(SHIPPED, ARRIVE, ARRIVED, BORROWING_LIBRARIAN),
			rule(ARRIVED, CHECK_OUT, ON_LOAN, BORROWING_LIBRARIAN),
			rule(ARRIVED, EXPIRE_PICKUP, UNCLAIMED, SYSTEM, BORROWING_LIBRARIAN),
			rule(ON_LOAN, CHECK_IN, RETURNED, BORROWING_LIBRARIAN),
			rule(ON_LOAN, REPORT_LOST, LOST, BORROWING_LIBRARIAN),
			rule(RETURNED, RETURN_SHIP, RETURN_SHIPPED, BORROWING_LIBRARIAN),
			rule(UNCLAIMED, RETURN_SHIP, RETURN_SHIPPED, BORROWING_LIBRARIAN),
			rule(RETURN_SHIPPED, COMPLETE, COMPLETED, LENDING_LIBRARIAN));

	private LoanRequestStateMachine() {
	}

	/** 신청. 신청자 본인만 할 수 있다. 사전 차단 검사는 신청 서비스가 먼저 한다. */
	public static LoanRequestStatus request(ActorRelation relation) {
		Objects.requireNonNull(relation, "relation");
		if (relation != REQUESTER) {
			throw new LoanRequestTransitionException(Rejection.NOT_PERMITTED,
					relation + "은(는) 상호대차를 신청할 수 없습니다");
		}
		return REQUESTED;
	}

	/**
	 * 추가 입력이 필요 없는 행동. 신청, 취소, 대출은 전용 메서드를 쓴다.
	 */
	public static LoanRequestStatus transit(LoanRequestStatus from, LoanRequestAction action, ActorRelation relation) {
		Objects.requireNonNull(action, "action");
		if (DEDICATED_ACTIONS.contains(action)) {
			throw new IllegalArgumentException(action + "은(는) 전용 메서드로 처리해야 합니다");
		}
		return resolve(from, action, relation).to();
	}

	/** 취소. 관계에 맞는 사유만 고를 수 있다. */
	public static LoanRequestStatus cancel(LoanRequestStatus from, CancelReasonCode reason, ActorRelation relation) {
		Objects.requireNonNull(reason, "reason");
		Rule rule = resolve(from, CANCEL, relation);
		if (!reason.getScope().permits(relation)) {
			throw new LoanRequestTransitionException(Rejection.CANCEL_REASON_NOT_ALLOWED,
					relation + "은(는) " + reason + " 사유로 취소할 수 없습니다");
		}
		return rule.to();
	}

	/** 대출 처리. 받는 사람은 신청자 본인이나 가족이어야 한다. */
	public static LoanRequestStatus checkOut(LoanRequestStatus from, ActorRelation relation, ActorRelation receiver) {
		Objects.requireNonNull(receiver, "receiver");
		Rule rule = resolve(from, CHECK_OUT, relation);
		if (!RECEIVERS.contains(receiver)) {
			throw new LoanRequestTransitionException(Rejection.INVALID_RECEIVER,
					receiver + "에게는 대출할 수 없습니다. 신청자 본인이나 가족만 받을 수 있습니다");
		}
		return rule.to();
	}

	/** 지금 이 관계의 사람이 할 수 있는 행동. 상세 화면의 버튼 목록에 쓴다. */
	public static Set<LoanRequestAction> availableActions(LoanRequestStatus status, ActorRelation relation) {
		Objects.requireNonNull(status, "status");
		Objects.requireNonNull(relation, "relation");
		Set<LoanRequestAction> actions = EnumSet.noneOf(LoanRequestAction.class);
		RULES.forEach((key, rule) -> {
			if (key.from() == status && rule.allowed().contains(relation)) {
				actions.add(key.action());
			}
		});
		return actions;
	}

	private static Rule resolve(LoanRequestStatus from, LoanRequestAction action, ActorRelation relation) {
		Objects.requireNonNull(from, "from");
		Objects.requireNonNull(relation, "relation");
		Rule rule = RULES.get(new Key(from, action));
		if (rule == null) {
			throw new LoanRequestTransitionException(Rejection.INVALID_STATE,
					from + " 상태에서는 " + action + " 할 수 없습니다");
		}
		if (!rule.allowed().contains(relation)) {
			throw new LoanRequestTransitionException(Rejection.NOT_PERMITTED,
					relation + "은(는) " + from + " 상태에서 " + action + " 할 수 없습니다");
		}
		return rule;
	}

	private static Map.Entry<Key, Rule> rule(LoanRequestStatus from, LoanRequestAction action,
			LoanRequestStatus to, ActorRelation first, ActorRelation... rest) {
		return Map.entry(new Key(from, action), new Rule(to, EnumSet.of(first, rest)));
	}
}
