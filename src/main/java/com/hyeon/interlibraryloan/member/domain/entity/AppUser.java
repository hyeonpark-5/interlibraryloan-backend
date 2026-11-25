package com.hyeon.interlibraryloan.member.domain.entity;

import com.hyeon.interlibraryloan.common.entity.BaseTimeEntity;
import com.hyeon.interlibraryloan.member.domain.type.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로그인 계정. 비밀번호는 Spring Security 단계에서 채운다.
 */
@Getter
@Entity
@Table(name = "app_user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppUser extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "app_user_seq")
	@SequenceGenerator(name = "app_user_seq", sequenceName = "app_user_seq", allocationSize = 50)
	private Long id;

	@Column(name = "login_id", nullable = false, length = 50)
	private String loginId;

	@Column(name = "password_hash", length = 100)
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false, length = 20)
	private UserRole role;

	@Column(name = "enabled", nullable = false)
	private boolean enabled;

	private AppUser(String loginId, UserRole role) {
		if (loginId == null || loginId.isBlank()) {
			throw new IllegalArgumentException("loginId는 비어 있을 수 없습니다");
		}
		this.loginId = loginId;
		this.role = Objects.requireNonNull(role, "role");
		this.enabled = true;
	}

	/** 비밀번호 없이 사용 가능한 계정을 만든다. */
	public static AppUser create(String loginId, UserRole role) {
		return new AppUser(loginId, role);
	}
}
