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
class InterlibraryloanApplicationTests {

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Test
	@DisplayName("애플리케이션이 테스트용 PostgreSQL 컨테이너 위에서 뜬다")
	void contextLoads() {
	}

	@Test
	@DisplayName("테스트 DB는 로컬과 같은 PostgreSQL 15다")
	void usesPostgres15() {
		String version = jdbcTemplate.queryForObject("show server_version", String.class);

		assertThat(version).startsWith("15.");
	}
}
