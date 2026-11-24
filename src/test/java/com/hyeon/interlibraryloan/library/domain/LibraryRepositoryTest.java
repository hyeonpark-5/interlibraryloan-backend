package com.hyeon.interlibraryloan.library.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hyeon.interlibraryloan.TestcontainersConfiguration;
import java.math.BigDecimal;
import java.util.List;
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
class LibraryRepositoryTest {

	// 서울 경계를 넉넉히 감싼 범위. 위도와 경도가 뒤바뀌면 여기서 걸린다.
	private static final BigDecimal SEOUL_MIN_LAT = new BigDecimal("37.40");
	private static final BigDecimal SEOUL_MAX_LAT = new BigDecimal("37.72");
	private static final BigDecimal SEOUL_MIN_LON = new BigDecimal("126.76");
	private static final BigDecimal SEOUL_MAX_LON = new BigDecimal("127.19");

	@Autowired
	LibraryRepository libraryRepository;

	@Autowired
	LibraryPolicyRepository libraryPolicyRepository;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Test
	@DisplayName("도서관 코드로 도서관과 자치구, 좌표를 읽는다")
	void findByCode() {
		// when
		Library library = libraryRepository.findByCode("MT").orElseThrow();

		// then
		assertThat(library.getName()).isEqualTo("정독도서관");
		assertThat(library.getDistrict().getName()).isEqualTo("종로구");
		assertThat(library.getLatitude()).isEqualByComparingTo("37.581031");
		assertThat(library.getLongitude()).isEqualByComparingTo("126.983694");
		assertThat(library.isInterlibraryEnabled()).isTrue();
		assertThat(library.getCreatedAt()).isNotNull();
	}

	@Test
	@DisplayName("모든 도서관의 좌표가 서울 범위 안에 있다 (위도, 경도가 뒤바뀌지 않았다)")
	void coordinatesAreInSeoul() {
		List<Library> libraries = libraryRepository.findAll();

		assertThat(libraries).hasSize(22).allSatisfy(library -> {
			assertThat(library.getLatitude()).isBetween(SEOUL_MIN_LAT, SEOUL_MAX_LAT);
			assertThat(library.getLongitude()).isBetween(SEOUL_MIN_LON, SEOUL_MAX_LON);
		});
	}

	@Test
	@DisplayName("도서관 정책은 도서관 id를 공유하고 기본값을 가진다")
	void policySharesLibraryId() {
		// given
		Library library = libraryRepository.findByCode("MA").orElseThrow();

		// when
		LibraryPolicy policy = libraryPolicyRepository.findById(library.getId()).orElseThrow();

		// then
		assertThat(policy.getLibrary().getCode()).isEqualTo("MA");
		assertThat(policy.getLoanDays()).isEqualTo(14);
		assertThat(policy.getNewBookBlockDays()).isZero();
	}

	@Test
	@DisplayName("도서관 코드는 중복될 수 없다")
	void libraryCodeIsUnique() {
		assertThatThrownBy(() -> jdbcTemplate.update("""
				insert into library (id, code, name, district_code, road_address, latitude, longitude,
				                     interlibrary_enabled, created_at, updated_at)
				values (nextval('library_seq'), 'MA', '중복 도서관', '11680', '주소', 37.5, 127.0, true, now(), now())
				"""))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	@DisplayName("도서관 정책의 대출기간은 0일 수 없다")
	void loanDaysMustBePositive() {
		assertThatThrownBy(() -> jdbcTemplate.update(
				"update library_policy set loan_days = 0 where library_id = 1"))
				.isInstanceOf(DataIntegrityViolationException.class);
	}
}
