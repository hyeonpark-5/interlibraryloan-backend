package com.hyeon.interlibraryloan.book.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 자료 유형 코드 테이블.
 * interlibraryAllowed가 false인 유형은 상호대차 신청이 사전 차단된다 (BR-01).
 */
@Getter
@Entity
@Table(name = "material_type")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MaterialType {

	@Id
	@Column(name = "code", length = 20)
	private String code;

	@Column(name = "name", nullable = false, length = 50)
	private String name;

	@Column(name = "interlibrary_allowed", nullable = false)
	private boolean interlibraryAllowed;
}
