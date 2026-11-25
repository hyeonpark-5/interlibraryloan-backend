package com.hyeon.interlibraryloan.member.domain.entity;

import com.hyeon.interlibraryloan.common.entity.BaseTimeEntity;
import com.hyeon.interlibraryloan.library.domain.entity.Library;
import com.hyeon.interlibraryloan.member.domain.type.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 직원(사서, 도서관 관리자, 시스템 관리자). 계정 id를 PK로 공유한다.
 * <p>
 * role은 계정 역할의 복사본이다. DB의 복합 외래키 (user_id, role)가 두 값이 항상 같도록 지킨다.
 */
@Getter
@Entity
@Table(name = "staff")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Staff extends BaseTimeEntity {

	@Id
	@Column(name = "user_id")
	private Long userId;

	@MapsId
	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id")
	private AppUser user;

	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false, length = 20)
	private UserRole role;

	@Column(name = "name", nullable = false, length = 50)
	private String name;

	/** 소속 도서관. 시스템 관리자는 없다 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "library_id")
	private Library library;

	private Staff(AppUser user, String name, Library library) {
		this.user = Objects.requireNonNull(user, "user");
		UserRole userRole = user.getRole();
		if (!userRole.isStaff()) {
			throw new IllegalArgumentException("이용자 계정으로는 직원을 만들 수 없습니다");
		}
		if (userRole.requiresLibrary() && library == null) {
			throw new IllegalArgumentException(userRole + "은(는) 소속 도서관이 있어야 합니다");
		}
		if (!userRole.requiresLibrary() && library != null) {
			throw new IllegalArgumentException(userRole + "은(는) 소속 도서관이 없어야 합니다");
		}
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("name은 비어 있을 수 없습니다");
		}
		this.role = userRole;
		this.name = name;
		this.library = library;
	}

	/**
	 * @param library 사서와 도서관 관리자는 필수, 시스템 관리자는 null
	 */
	public static Staff create(AppUser user, String name, Library library) {
		return new Staff(user, name, library);
	}
}
