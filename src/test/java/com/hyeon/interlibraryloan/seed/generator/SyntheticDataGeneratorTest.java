package com.hyeon.interlibraryloan.seed.generator;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyeon.interlibraryloan.member.domain.type.PatronStatus;
import com.hyeon.interlibraryloan.member.domain.type.UserRole;
import com.hyeon.interlibraryloan.seed.config.SeedProperties;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SyntheticDataGeneratorTest {

	private static final List<String> LIBRARY_CODES = List.of(
			"MA", "MB", "MC", "MD", "ME", "MF", "MG", "MH", "MJ", "MK", "ML",
			"MM", "MN", "MP", "MQ", "MR", "MS", "MT", "MU", "MV", "MW", "MX");
	private static final LocalDate REFERENCE = LocalDate.of(2026, 11, 25);

	private static SeedProperties properties(long seed, double suspendedRatio) {
		return new SeedProperties(seed, REFERENCE, 30, 2, suspendedRatio);
	}

	private static final SeedPlan PLAN = SyntheticDataGenerator.generate(LIBRARY_CODES, properties(42, 0.05));

	@Test
	@DisplayName("같은 시드면 몇 번을 돌려도 같은 데이터가 나온다")
	void deterministic() {
		assertThat(SyntheticDataGenerator.generate(LIBRARY_CODES, properties(42, 0.05))).isEqualTo(PLAN);
	}

	@Test
	@DisplayName("도서관 목록의 순서가 달라도 같은 데이터가 나온다")
	void independentOfInputOrder() {
		List<String> shuffled = new ArrayList<>(LIBRARY_CODES);
		Collections.reverse(shuffled);

		assertThat(SyntheticDataGenerator.generate(shuffled, properties(42, 0.05))).isEqualTo(PLAN);
	}

	@Test
	@DisplayName("시드가 다르면 다른 데이터가 나온다")
	void differentSeed() {
		assertThat(SyntheticDataGenerator.generate(LIBRARY_CODES, properties(43, 0.05))).isNotEqualTo(PLAN);
	}

	@Test
	@DisplayName("시스템 관리자 1명, 도서관마다 관리자 1명과 사서 2명, 이용자 30명을 만든다")
	void counts() {
		assertThat(PLAN.staff()).hasSize(1 + 22 * 3);
		assertThat(PLAN.patrons()).hasSize(22 * 30);

		assertThat(PLAN.staff()).filteredOn(s -> s.role() == UserRole.SYSTEM_ADMIN)
				.singleElement()
				.satisfies(admin -> {
					assertThat(admin.loginId()).isEqualTo("admin");
					assertThat(admin.libraryCode()).isNull();
				});

		Map<String, Long> managers = countByLibrary(PLAN.staff().stream().filter(s -> s.role() == UserRole.LIBRARY_MANAGER));
		Map<String, Long> librarians = countByLibrary(PLAN.staff().stream().filter(s -> s.role() == UserRole.LIBRARIAN));
		Map<String, Long> patrons = PLAN.patrons().stream()
				.collect(Collectors.groupingBy(PatronSeed::libraryCode, Collectors.counting()));
		assertThat(managers).hasSize(22).allSatisfy((code, count) -> assertThat(count).isEqualTo(1));
		assertThat(librarians).hasSize(22).allSatisfy((code, count) -> assertThat(count).isEqualTo(2));
		assertThat(patrons).hasSize(22).allSatisfy((code, count) -> assertThat(count).isEqualTo(30));
	}

	@Test
	@DisplayName("로그인 아이디와 회원번호는 겹치지 않는다")
	void uniqueIdentifiers() {
		List<String> loginIds = Stream.concat(
				PLAN.staff().stream().map(StaffSeed::loginId),
				PLAN.patrons().stream().map(PatronSeed::loginId)).toList();

		assertThat(loginIds).doesNotHaveDuplicates();
		assertThat(PLAN.patrons()).extracting(PatronSeed::cardNumber)
				.doesNotHaveDuplicates()
				.allMatch(card -> card.matches("M[A-Z]\\d{6}"));
	}

	@Test
	@DisplayName("실제 개인정보와 겹치지 않도록 이메일과 전화번호는 영문 소문자 임의 문자열이다")
	void contactsAreMeaninglessLetters() {
		assertThat(PLAN.patrons()).allSatisfy(patron -> {
			assertThat(patron.email()).matches("[a-z]{12}");
			assertThat(patron.phone()).matches("[a-z]{10}");
		});
	}

	@Test
	@DisplayName("생년월일은 기준일로부터 7세에서 80세 사이다")
	void birthDateRange() {
		assertThat(PLAN.patrons()).extracting(PatronSeed::birthDate)
				.allMatch(date -> !date.isBefore(REFERENCE.minusYears(80)) && !date.isAfter(REFERENCE.minusYears(7)));
	}

	@Test
	@DisplayName("정지된 이용자만 정지 종료일이 있고, 종료일은 기준일 이후 30일 안이다")
	void suspendedUntil() {
		assertThat(PLAN.patrons()).allSatisfy(patron -> {
			if (patron.status() == PatronStatus.SUSPENDED) {
				assertThat(patron.suspendedUntil()).isAfter(REFERENCE).isBeforeOrEqualTo(REFERENCE.plusDays(30));
			} else {
				assertThat(patron.status()).isEqualTo(PatronStatus.ACTIVE);
				assertThat(patron.suspendedUntil()).isNull();
			}
		});
		assertThat(PLAN.patrons()).anyMatch(p -> p.status() == PatronStatus.SUSPENDED);
	}

	@Test
	@DisplayName("정지 비율이 0이면 아무도, 1이면 모두 정지된다")
	void suspendedRatioBounds() {
		assertThat(SyntheticDataGenerator.generate(LIBRARY_CODES, properties(42, 0)).patrons())
				.allMatch(p -> p.status() == PatronStatus.ACTIVE);
		assertThat(SyntheticDataGenerator.generate(LIBRARY_CODES, properties(42, 1)).patrons())
				.allMatch(p -> p.status() == PatronStatus.SUSPENDED);
	}

	private static Map<String, Long> countByLibrary(Stream<StaffSeed> staff) {
		return staff.collect(Collectors.groupingBy(StaffSeed::libraryCode, Collectors.counting()));
	}
}
