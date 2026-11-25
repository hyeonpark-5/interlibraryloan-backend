package com.hyeon.interlibraryloan.member.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hyeon.interlibraryloan.TestcontainersConfiguration;
import com.hyeon.interlibraryloan.library.domain.entity.Library;
import com.hyeon.interlibraryloan.library.domain.repository.LibraryRepository;
import com.hyeon.interlibraryloan.member.domain.entity.AppUser;
import com.hyeon.interlibraryloan.member.domain.entity.Patron;
import com.hyeon.interlibraryloan.member.domain.entity.Staff;
import com.hyeon.interlibraryloan.member.domain.type.PatronStatus;
import com.hyeon.interlibraryloan.member.domain.type.UserRole;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Import(TestcontainersConfiguration.class)
class MemberRepositoryTest {

	@Autowired
	AppUserRepository appUserRepository;

	@Autowired
	StaffRepository staffRepository;

	@Autowired
	PatronRepository patronRepository;

	@Autowired
	LibraryRepository libraryRepository;

	@Autowired
	EntityManager entityManager;

	@Autowired
	JdbcTemplate jdbcTemplate;

	Library gangnam;
	Library jeongdok;

	@BeforeEach
	void setUp() {
		gangnam = libraryRepository.findByCode("MA").orElseThrow();
		jeongdok = libraryRepository.findByCode("MT").orElseThrow();
	}

	private AppUser savedUser(String loginId, UserRole role) {
		return appUserRepository.saveAndFlush(AppUser.create(loginId, role));
	}

	private Patron patron(Library library, String cardNumber) {
		return Patron.builder()
				.library(library)
				.cardNumber(cardNumber)
				.name("박이용")
				.status(PatronStatus.ACTIVE)
				.build();
	}

	@Test
	@DisplayName("직원은 계정 id를 PK로 공유하고 다시 읽어도 역할과 도서관이 그대로다")
	void staffSharesUserId() {
		// given
		AppUser user = savedUser("ma-test-librarian", UserRole.LIBRARIAN);
		staffRepository.save(Staff.create(user, "김사서", gangnam));
		entityManager.flush();
		entityManager.clear();

		// when
		Staff found = staffRepository.findById(user.getId()).orElseThrow();

		// then
		assertThat(found.getRole()).isEqualTo(UserRole.LIBRARIAN);
		assertThat(found.getLibrary().getCode()).isEqualTo("MA");
		assertThat(found.getUser().getLoginId()).isEqualTo("ma-test-librarian");
	}

	@Test
	@DisplayName("DB는 소속 도서관이 없는 사서를 막는다")
	void dbRejectsLibrarianWithoutLibrary() {
		AppUser user = savedUser("no-library", UserRole.LIBRARIAN);

		assertThatThrownBy(() -> jdbcTemplate.update("""
				insert into staff (user_id, role, name, library_id, created_at, updated_at)
				values (?, 'LIBRARIAN', '김사서', null, now(), now())
				""", user.getId()))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	@DisplayName("DB는 소속 도서관이 있는 시스템 관리자를 막는다")
	void dbRejectsSystemAdminWithLibrary() {
		AppUser user = savedUser("admin-with-library", UserRole.SYSTEM_ADMIN);

		assertThatThrownBy(() -> jdbcTemplate.update("""
				insert into staff (user_id, role, name, library_id, created_at, updated_at)
				values (?, 'SYSTEM_ADMIN', '관리자', ?, now(), now())
				""", user.getId(), gangnam.getId()))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	@DisplayName("DB는 계정 역할과 다른 직원 역할을 막는다 (복합 외래키)")
	void dbRejectsRoleMismatch() {
		AppUser user = savedUser("manager-account", UserRole.LIBRARY_MANAGER);

		assertThatThrownBy(() -> jdbcTemplate.update("""
				insert into staff (user_id, role, name, library_id, created_at, updated_at)
				values (?, 'LIBRARIAN', '김사서', ?, now(), now())
				""", user.getId(), gangnam.getId()))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	@DisplayName("계정 역할을 바꾸면 직원 역할도 같이 바뀐다")
	void roleChangeCascadesToStaff() {
		// given
		AppUser user = savedUser("promoted", UserRole.LIBRARIAN);
		staffRepository.saveAndFlush(Staff.create(user, "김사서", gangnam));

		// when
		jdbcTemplate.update("update app_user set role = 'LIBRARY_MANAGER' where id = ?", user.getId());

		// then
		assertThat(jdbcTemplate.queryForObject("select role from staff where user_id = ?", String.class, user.getId()))
				.isEqualTo("LIBRARY_MANAGER");
	}

	@Test
	@DisplayName("직원 계정을 이용자 역할로 바꿀 수 없다")
	void staffAccountCannotBecomePatron() {
		AppUser user = savedUser("stays-staff", UserRole.LIBRARIAN);
		staffRepository.saveAndFlush(Staff.create(user, "김사서", gangnam));

		assertThatThrownBy(() -> jdbcTemplate.update(
				"update app_user set role = 'PATRON' where id = ?", user.getId()))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	@DisplayName("회원번호는 도서관 안에서만 유일하다")
	void cardNumberIsUniquePerLibrary() {
		patronRepository.saveAndFlush(patron(gangnam, "SAME-CARD"));

		// 다른 도서관은 같은 회원번호를 쓸 수 있다
		patronRepository.saveAndFlush(patron(jeongdok, "SAME-CARD"));

		// 같은 도서관에서는 안 된다
		assertThatThrownBy(() -> patronRepository.saveAndFlush(patron(gangnam, "SAME-CARD")))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	@DisplayName("로그인 아이디는 중복될 수 없다")
	void loginIdIsUnique() {
		savedUser("duplicated", UserRole.PATRON);

		assertThatThrownBy(() -> savedUser("duplicated", UserRole.PATRON))
				.isInstanceOf(DataIntegrityViolationException.class);
	}
}
