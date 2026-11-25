package com.hyeon.interlibraryloan.seed.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyeon.interlibraryloan.TestcontainersConfiguration;
import com.hyeon.interlibraryloan.library.domain.entity.Library;
import com.hyeon.interlibraryloan.library.domain.repository.LibraryRepository;
import com.hyeon.interlibraryloan.member.domain.entity.AppUser;
import com.hyeon.interlibraryloan.member.domain.repository.AppUserRepository;
import com.hyeon.interlibraryloan.member.domain.repository.PatronRepository;
import com.hyeon.interlibraryloan.member.domain.repository.StaffRepository;
import com.hyeon.interlibraryloan.member.domain.type.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

/**
 * 합성 데이터를 실제 DB에 넣는다. 테스트가 끝나면 롤백되어 다른 테스트에 영향을 주지 않는다.
 */
@SpringBootTest
@Transactional
@Import(TestcontainersConfiguration.class)
class SyntheticDataSeederTest {

	@Autowired
	SyntheticDataSeeder seeder;

	@Autowired
	AppUserRepository appUserRepository;

	@Autowired
	StaffRepository staffRepository;

	@Autowired
	PatronRepository patronRepository;

	@Autowired
	LibraryRepository libraryRepository;

	@Test
	@DisplayName("빈 DB에 직원 67명, 이용자 660명과 그 계정을 넣는다")
	void seed() {
		// when
		SeedResult result = seeder.seed();

		// then
		assertThat(result).isEqualTo(SeedResult.created(67, 660));
		assertThat(appUserRepository.count()).isEqualTo(67 + 660);
		assertThat(staffRepository.count()).isEqualTo(67);
		assertThat(patronRepository.count()).isEqualTo(660);

		Library gangnam = libraryRepository.findByCode("MA").orElseThrow();
		assertThat(staffRepository.findByLibraryIdAndRole(gangnam.getId(), UserRole.LIBRARIAN)).hasSize(2);
		assertThat(staffRepository.findByLibraryIdAndRole(gangnam.getId(), UserRole.LIBRARY_MANAGER)).hasSize(1);
		assertThat(patronRepository.countByLibraryId(gangnam.getId())).isEqualTo(30);

		AppUser admin = appUserRepository.findByLoginId("admin").orElseThrow();
		assertThat(admin.getRole()).isEqualTo(UserRole.SYSTEM_ADMIN);
		assertThat(staffRepository.findById(admin.getId()).orElseThrow().getLibrary()).isNull();
	}

	@Test
	@DisplayName("이미 계정이 있으면 아무것도 넣지 않는다")
	void skipWhenAlreadySeeded() {
		seeder.seed();
		long before = appUserRepository.count();

		SeedResult second = seeder.seed();

		assertThat(second.skipped()).isTrue();
		assertThat(appUserRepository.count()).isEqualTo(before);
	}
}
