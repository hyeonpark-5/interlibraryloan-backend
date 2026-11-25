package com.hyeon.interlibraryloan.member.domain.entity;

import com.hyeon.interlibraryloan.common.entity.BaseTimeEntity;
import com.hyeon.interlibraryloan.library.domain.entity.Library;
import com.hyeon.interlibraryloan.member.domain.type.PatronStatus;
import com.hyeon.interlibraryloan.member.domain.type.UserRole;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 이용자. 한 도서관에 소속되어 그 도서관을 통해서만 신청하고 수령한다.
 */
@Getter
@Entity
@Table(name = "patron")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Patron extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "patron_seq")
	@SequenceGenerator(name = "patron_seq", sequenceName = "patron_seq", allocationSize = 50)
	private Long id;

	/** 소속 도서관. 신청하면 이 도서관이 요청관이 된다 */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "library_id", nullable = false)
	private Library library;

	/** 회원번호. 도서관 안에서만 유일하다 */
	@Column(name = "card_number", nullable = false, length = 30)
	private String cardNumber;

	@Column(name = "name", nullable = false, length = 50)
	private String name;

	@Column(name = "email", length = 100)
	private String email;

	@Column(name = "phone", length = 20)
	private String phone;

	@Column(name = "birth_date")
	private LocalDate birthDate;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private PatronStatus status;

	@Column(name = "suspended_until")
	private LocalDate suspendedUntil;

	/** 온라인 계정. 데스크에서만 등록하고 가입하지 않은 이용자는 없다 */
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "app_user_id")
	private AppUser appUser;

	@Builder
	private Patron(Library library, String cardNumber, String name, String email, String phone,
			LocalDate birthDate, PatronStatus status, LocalDate suspendedUntil, AppUser appUser) {
		this.library = Objects.requireNonNull(library, "library");
		this.cardNumber = Objects.requireNonNull(cardNumber, "cardNumber");
		this.name = Objects.requireNonNull(name, "name");
		this.email = email;
		this.phone = phone;
		this.birthDate = birthDate;
		this.status = Objects.requireNonNull(status, "status");
		this.suspendedUntil = suspendedUntil;
		if (appUser != null && appUser.getRole() != UserRole.PATRON) {
			throw new IllegalArgumentException("이용자 계정이 아닌 계정은 연결할 수 없습니다");
		}
		this.appUser = appUser;
	}
}
