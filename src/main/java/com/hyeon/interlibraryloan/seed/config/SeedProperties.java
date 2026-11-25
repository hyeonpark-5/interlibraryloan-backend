package com.hyeon.interlibraryloan.seed.config;

import java.time.LocalDate;
import java.util.Objects;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 합성 데이터 설정. application.yaml의 seed 항목에서 읽는다.
 *
 * @param randomSeed          난수 시드. 같으면 항상 같은 데이터가 나온다
 * @param referenceDate       생년월일, 정지 종료일 계산의 기준일. 실행 날짜에 따라 결과가 바뀌지 않게 고정한다
 * @param patronsPerLibrary   도서관마다 만들 이용자 수
 * @param librariansPerLibrary 도서관마다 만들 사서 수. 도서관 관리자 1명은 따로 만든다
 * @param suspendedRatio      대출 정지 상태로 만들 이용자 비율
 */
@ConfigurationProperties("seed")
public record SeedProperties(
		long randomSeed,
		LocalDate referenceDate,
		int patronsPerLibrary,
		int librariansPerLibrary,
		double suspendedRatio) {

	public SeedProperties {
		Objects.requireNonNull(referenceDate, "seed.reference-date");
		if (patronsPerLibrary < 0 || librariansPerLibrary < 0) {
			throw new IllegalArgumentException("인원 수는 0 이상이어야 합니다");
		}
		if (suspendedRatio < 0 || suspendedRatio > 1) {
			throw new IllegalArgumentException("seed.suspended-ratio는 0과 1 사이여야 합니다");
		}
	}
}
