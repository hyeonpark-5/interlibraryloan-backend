package com.hyeon.interlibraryloan.library.domain.entity;

import com.hyeon.interlibraryloan.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 도서관별 상호대차 정책. 도서관과 1:1이며 도서관 id를 PK로 공유한다.
 */
@Getter
@Entity
@Table(name = "library_policy")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LibraryPolicy extends BaseTimeEntity {

	@Id
	@Column(name = "library_id")
	private Long libraryId;

	@MapsId
	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "library_id")
	private Library library;

	/** 대출기간(일). 수령 시점부터 센다. */
	@Column(name = "loan_days", nullable = false)
	private int loanDays;

	/** 이용자 기준 진행 중 요청의 최대 건수. */
	@Column(name = "max_active_requests", nullable = false)
	private int maxActiveRequests;

	/** 신청 후 발송 기한(일). */
	@Column(name = "ship_deadline_days", nullable = false)
	private int shipDeadlineDays;

	/** 도착 후 수령 기한(일). */
	@Column(name = "pickup_deadline_days", nullable = false)
	private int pickupDeadlineDays;

	/** 입수 후 이 기간 안의 자료는 상호대차 불가. 0이면 제한 없음. */
	@Column(name = "new_book_block_days", nullable = false)
	private int newBookBlockDays;
}
