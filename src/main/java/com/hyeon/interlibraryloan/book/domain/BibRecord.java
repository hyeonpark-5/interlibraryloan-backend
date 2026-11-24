package com.hyeon.interlibraryloan.book.domain;

import com.hyeon.interlibraryloan.common.domain.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 서지. 책의 추상적인 정보이며, 이용자는 서지를 보고 신청한다.
 * 같은 ISBN-13은 서지 하나로 묶는다.
 */
@Getter
@Entity
@Table(name = "bib_record")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BibRecord extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bib_record_seq")
	@SequenceGenerator(name = "bib_record_seq", sequenceName = "bib_record_seq", allocationSize = 50)
	private Long id;

	@Column(name = "isbn13", nullable = false, length = 13)
	private String isbn13;

	@Column(name = "title", nullable = false, length = 500)
	private String title;

	/** 편권차. 여러 권짜리 책의 권 번호 */
	@Column(name = "volume", length = 20)
	private String volume;

	/** 편제. 권의 제목 */
	@Column(name = "volume_title", length = 200)
	private String volumeTitle;

	@Column(name = "author", length = 500)
	private String author;

	@Column(name = "publisher", length = 200)
	private String publisher;

	@Column(name = "published_year")
	private Short publishedYear;

	/** 한국십진분류 번호. 청구기호 앞자리에서 뽑는다. */
	@Column(name = "kdc", length = 20)
	private String kdc;

	@Builder
	private BibRecord(Isbn isbn, String title, String volume, String volumeTitle,
			String author, String publisher, Short publishedYear, String kdc) {
		this.isbn13 = Objects.requireNonNull(isbn, "isbn").value();
		this.title = requireText(title, "title");
		this.volume = volume;
		this.volumeTitle = volumeTitle;
		this.author = author;
		this.publisher = publisher;
		this.publishedYear = publishedYear;
		this.kdc = kdc;
	}

	public Isbn getIsbn() {
		return new Isbn(isbn13);
	}

	private static String requireText(String value, String name) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(name + "은(는) 비어 있을 수 없습니다");
		}
		return value;
	}
}
