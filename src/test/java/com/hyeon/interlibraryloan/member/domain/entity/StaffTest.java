package com.hyeon.interlibraryloan.member.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hyeon.interlibraryloan.TestcontainersConfiguration;
import com.hyeon.interlibraryloan.library.domain.entity.Library;
import com.hyeon.interlibraryloan.library.domain.repository.LibraryRepository;
import com.hyeon.interlibraryloan.member.domain.type.PatronStatus;
import com.hyeon.interlibraryloan.member.domain.type.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

/**
 * 직원, 이용자 엔티티의 생성 규칙. 도서관을 DB에서 읽어야 해서 컨테이너 위에서 돈다.
 */
@SpringBootTest
@Transactional
@Import(TestcontainersConfiguration.class)
class StaffTest {

	@Autowired
	LibraryRepository libraryRepository;

	Library gangnam;

	@BeforeEach
	void setUp() {
		gangnam = libraryRepository.findByCode("MA").orElseThrow();
	}

	@Test
	@DisplayName("직원은 계정의 역할을 그대로 가진다")
	void copiesRoleFromUser() {
		Staff staff = Staff.create(AppUser.create("ma-test", UserRole.LIBRARY_MANAGER), "김관리", gangnam);

		assertThat(staff.getRole()).isEqualTo(UserRole.LIBRARY_MANAGER);
		assertThat(staff.getLibrary()).isSameAs(gangnam);
	}

	@Test
	@DisplayName("사서와 도서관 관리자는 소속 도서관이 있어야 한다")
	void librarianRequiresLibrary() {
		assertThatThrownBy(() -> Staff.create(AppUser.create("t1", UserRole.LIBRARIAN), "김사서", null))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> Staff.create(AppUser.create("t2", UserRole.LIBRARY_MANAGER), "김관리", null))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	@DisplayName("시스템 관리자는 소속 도서관이 없어야 한다")
	void systemAdminHasNoLibrary() {
		assertThat(Staff.create(AppUser.create("admin-t", UserRole.SYSTEM_ADMIN), "관리자", null).getLibrary())
				.isNull();
		assertThatThrownBy(() -> Staff.create(AppUser.create("admin-t2", UserRole.SYSTEM_ADMIN), "관리자", gangnam))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	@DisplayName("이용자 계정으로는 직원을 만들 수 없다")
	void patronAccountIsNotStaff() {
		assertThatThrownBy(() -> Staff.create(AppUser.create("p1", UserRole.PATRON), "이용자", gangnam))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	@DisplayName("이용자에는 이용자 계정만 연결할 수 있다")
	void patronAcceptsOnlyPatronAccount() {
		assertThatThrownBy(() -> Patron.builder()
				.library(gangnam)
				.cardNumber("MA999999")
				.name("박이용")
				.status(PatronStatus.ACTIVE)
				.appUser(AppUser.create("ma-librarian-x", UserRole.LIBRARIAN))
				.build())
				.isInstanceOf(IllegalArgumentException.class);
	}
}
