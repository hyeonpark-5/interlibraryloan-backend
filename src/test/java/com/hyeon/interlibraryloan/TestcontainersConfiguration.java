package com.hyeon.interlibraryloan;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * 테스트용 PostgreSQL 컨테이너.
 * 로컬 Homebrew PostgreSQL과 같은 메이저 버전(15)을 쓴다.
 * {@link ServiceConnection}이 컨테이너 접속 정보를 DataSource에 자동으로 연결한다.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

	@Bean
	@ServiceConnection
	PostgreSQLContainer postgresContainer() {
		return new PostgreSQLContainer("postgres:15-alpine");
	}
}
