package com.hyeon.interlibraryloan.book.domain;

import com.hyeon.interlibraryloan.common.domain.BaseTimeEntity;
import com.hyeon.interlibraryloan.library.domain.Library;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 소장 자료. 특정 도서관이 가진 실물 한 권이며, 등록번호로 구분한다.
 * 상태 전환(배정, 발송, 회송 등)은 상호대차 상태머신 단계에서 행위 메서드로 추가한다.
 */
@Getter
@Entity
@Table(name = "item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "item_seq")
	@SequenceGenerator(name = "item_seq", sequenceName = "item_seq", allocationSize = 50)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "library_id", nullable = false)
	private Library library;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "bib_record_id", nullable = false)
	private BibRecord bibRecord;

	/** 소장도서정보 API의 책 레코드키. 재적재할 때 같은 자료를 찾는 데 쓴다. */
	@Column(name = "source_book_key")
	private Long sourceBookKey;

	/** 등록번호 */
	@Column(name = "barcode", nullable = false, length = 30)
	private String barcode;

	/** 청구기호. 발송 준비 목록의 서가 위치 */
	@Column(name = "call_number", length = 100)
	private String callNumber;

	/** 소장 자료실 */
	@Column(name = "shelf_location", length = 100)
	private String shelfLocation;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "material_type_code", nullable = false)
	private MaterialType materialType;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private ItemStatus status;

	@Column(name = "interlibrary_lendable", nullable = false)
	private boolean interlibraryLendable;

	@Column(name = "acquired_at")
	private LocalDate acquiredAt;

	@Version
	@Column(name = "version", nullable = false)
	private Long version;

	@Builder
	private Item(Library library, BibRecord bibRecord, Long sourceBookKey, String barcode,
			String callNumber, String shelfLocation, MaterialType materialType,
			ItemStatus status, boolean interlibraryLendable, LocalDate acquiredAt) {
		this.library = Objects.requireNonNull(library, "library");
		this.bibRecord = Objects.requireNonNull(bibRecord, "bibRecord");
		this.sourceBookKey = sourceBookKey;
		this.barcode = Objects.requireNonNull(barcode, "barcode");
		this.callNumber = callNumber;
		this.shelfLocation = shelfLocation;
		this.materialType = Objects.requireNonNull(materialType, "materialType");
		this.status = Objects.requireNonNull(status, "status");
		this.interlibraryLendable = interlibraryLendable;
		this.acquiredAt = acquiredAt;
	}

	/** 이 자료를 상호대차로 내줄 수 없는 이유. 비어 있으면 배정 가능하다. */
	public Set<InterlibraryIneligibleReason> interlibraryIneligibleReasons(int newBookBlockDays, LocalDate today) {
		return InterlibraryEligibility.evaluate(
				status, interlibraryLendable, materialType.isInterlibraryAllowed(),
				acquiredAt, newBookBlockDays, today);
	}

	public boolean isInterlibraryEligible(int newBookBlockDays, LocalDate today) {
		return interlibraryIneligibleReasons(newBookBlockDays, today).isEmpty();
	}
}
