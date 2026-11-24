package com.hyeon.interlibraryloan;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ReferenceDataMigrationTest {

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Test
	@DisplayName("Flyway 마이그레이션이 실패 없이 모두 적용된다")
	void allMigrationsApplied() {
		Integer failed = jdbcTemplate.queryForObject(
				"select count(*) from flyway_schema_history where not success", Integer.class);
		Integer applied = jdbcTemplate.queryForObject(
				"select count(*) from flyway_schema_history where success and version is not null", Integer.class);

		assertThat(failed).isZero();
		assertThat(applied).isGreaterThanOrEqualTo(10);
	}

	@Test
	@DisplayName("서울 자치구 25개가 들어간다")
	void districtsSeeded() {
		assertThat(count("district")).isEqualTo(25);
	}

	@Test
	@DisplayName("BR-01 자료 유형 5개 중 참고자료, 귀중본, 연속간행물은 상호대차 불가다")
	void materialTypesSeeded() {
		assertThat(count("material_type")).isEqualTo(5);
		assertThat(jdbcTemplate.queryForList(
				"select code from material_type where not interlibrary_allowed order by code", String.class))
				.containsExactly("PERIODICAL", "RARE", "REFERENCE");
	}

	@Test
	@DisplayName("취소 사유 6개가 들어가고, 메모 필수는 OTHER뿐이다")
	void cancelReasonsSeeded() {
		assertThat(count("cancel_reason")).isEqualTo(6);
		assertThat(jdbcTemplate.queryForList(
				"select code from cancel_reason where memo_required", String.class))
				.containsExactly("OTHER");
	}

	@Test
	@DisplayName("교육청 소속 도서관 22곳과 각 도서관의 정책이 들어간다")
	void librariesAndPoliciesSeeded() {
		assertThat(count("library")).isEqualTo(22);
		assertThat(count("library_policy")).isEqualTo(22);
	}

	private int count(String table) {
		return jdbcTemplate.queryForObject("select count(*) from " + table, Integer.class);
	}
}
