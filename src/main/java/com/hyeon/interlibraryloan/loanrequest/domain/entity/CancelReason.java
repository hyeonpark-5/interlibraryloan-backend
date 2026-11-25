package com.hyeon.interlibraryloan.loanrequest.domain.entity;

import com.hyeon.interlibraryloan.loanrequest.domain.type.CancelReasonActorScope;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 상호대차 요청 취소 사유 코드 테이블.
 * 이용자에게는 patronMessage만 보내고, 도서관 간 메모는 보내지 않는다 (BR-06).
 */
@Getter
@Entity
@Table(name = "cancel_reason")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CancelReason {

	@Id
	@Column(name = "code", length = 30)
	private String code;

	@Column(name = "name", nullable = false, length = 50)
	private String name;

	@Column(name = "patron_message", nullable = false, length = 300)
	private String patronMessage;

	@Enumerated(EnumType.STRING)
	@Column(name = "actor_scope", nullable = false, length = 20)
	private CancelReasonActorScope actorScope;

	@Column(name = "memo_required", nullable = false)
	private boolean memoRequired;
}
