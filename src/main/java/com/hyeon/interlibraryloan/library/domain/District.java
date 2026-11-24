package com.hyeon.interlibraryloan.library.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 서울 자치구. code는 행정표준 시군구 코드다.
 * Flyway 기준 데이터로만 들어가는 읽기 전용 코드 테이블이다.
 */
@Getter
@Entity
@Table(name = "district")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class District {

	@Id
	@Column(name = "code", length = 10)
	private String code;

	@Column(name = "name", nullable = false, length = 20)
	private String name;
}
