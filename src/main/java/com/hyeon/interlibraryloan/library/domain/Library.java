package com.hyeon.interlibraryloan.library.domain;

import com.hyeon.interlibraryloan.common.domain.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 상호대차 참여 도서관.
 * code는 소장도서정보 API의 도서관 관리 코드(MA~MX)다.
 * 지금은 Flyway 기준 데이터로만 생성된다.
 */
@Getter
@Entity
@Table(name = "library")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Library extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "library_seq")
	@SequenceGenerator(name = "library_seq", sequenceName = "library_seq", allocationSize = 50)
	private Long id;

	@Column(name = "code", nullable = false, length = 10)
	private String code;

	@Column(name = "name", nullable = false, length = 100)
	private String name;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "district_code", nullable = false)
	private District district;

	@Column(name = "road_address", nullable = false)
	private String roadAddress;

	@Column(name = "latitude", nullable = false, precision = 9, scale = 6)
	private BigDecimal latitude;

	@Column(name = "longitude", nullable = false, precision = 9, scale = 6)
	private BigDecimal longitude;

	@Column(name = "phone", length = 20)
	private String phone;

	@Column(name = "homepage_url", length = 300)
	private String homepageUrl;

	@Column(name = "interlibrary_enabled", nullable = false)
	private boolean interlibraryEnabled;
}
